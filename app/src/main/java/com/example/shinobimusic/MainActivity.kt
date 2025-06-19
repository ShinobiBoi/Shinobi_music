package com.example.shinobimusic


import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.utilits.glideSong
import com.example.shinobimusic.databinding.ActivityMainBinding
import com.example.shinobimusic.service.MusicPlaybackService
import com.example.shinobimusic.ui.songs.SongsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import org.json.JSONObject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val PREFS_NAME = "shinobi_music_prefs"
    private val LAST_SONG_KEY = "last_played_song"
    private var repeatMode = RepeatMode.OFF

     private lateinit var navController: NavController


     val viewModel: SongsViewModel by viewModels()
    private lateinit var currentSongs:List<Song>
    private var currentSong:Song?=null


    private lateinit var binding: ActivityMainBinding




    private var mediaController: MediaController? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                binding.noPermisionContent.visibility=View.GONE
                binding.fragmentContainerView.visibility=View.VISIBLE
                viewModel.loadSongs()
            } else {
                binding.noPermisionContent.visibility=View.VISIBLE
                binding.fragmentContainerView.visibility=View.GONE
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }



       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           binding = ActivityMainBinding.inflate(layoutInflater)
           setContentView(binding.root)


           val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
           navController = navHostFragment.navController

           navController.addOnDestinationChangedListener{ _, destination, _ ->
               if (destination.id == R.id.songDetailFragment) {
                   binding.bottomPlayerContainer.bottomPlayerContent.visibility = View.GONE
               } else if (currentSong!=null && destination.id != R.id.songDetailFragment) {
                   binding.bottomPlayerContainer.bottomPlayerContent.visibility = View.VISIBLE
               }


           }

          iniztialcheck()

           binding.noPermisionBtn.setOnClickListener {
               checkPermission()
           }
           binding.bottomPlayerContainer.bottomPlayerNavigate.setOnClickListener {
                 // Your logic to get the current song
               currentSong?.let {
                   val action = NavigationgraphDirections.actionGlobalSongDetailFragment(it)
                   navController.navigate(action)
               }
           }



           observeSongs()
           setBottomPlayer()

       }

    private fun iniztialcheck() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                binding.noPermisionContent.visibility = View.GONE
                binding.fragmentContainerView.visibility = View.VISIBLE
                viewModel.loadSongs()

            }
        }
    }


    private fun setBottomPlayer() {

        loadLastPlayedSong()
        currentSong?.let { updateSong(it) }


        binding.bottomPlayerContainer.playerBottomPlayPause.setOnClickListener {
            mediaController?.let {
                if (it.isPlaying) it.pause() else it.play()
            }
        }
        binding.bottomPlayerContainer.playerBottomModeBtn.setOnClickListener {
            repeatMode = when (repeatMode) {
                RepeatMode.OFF -> {
                    binding.bottomPlayerContainer.playerBottomModeBtn.setImageResource(R.drawable.repeatall)
                    mediaController?.repeatMode = Player.REPEAT_MODE_ALL
                    RepeatMode.ALL
                }
                RepeatMode.ALL -> {
                    binding.bottomPlayerContainer.playerBottomModeBtn.setImageResource(R.drawable.repeatone)
                    mediaController?.repeatMode = Player.REPEAT_MODE_ONE
                    RepeatMode.ONE
                }
                RepeatMode.ONE -> {
                    binding.bottomPlayerContainer.playerBottomModeBtn.setImageResource(R.drawable.repeatoff)
                    mediaController?.repeatMode = Player.REPEAT_MODE_OFF
                    RepeatMode.OFF
                }
            }
        }

        if (mediaController?.isPlaying == true) {
            binding.bottomPlayerContainer.playerBottomPlayPause.setImageResource(R.drawable.pause)
        } else {
            binding.bottomPlayerContainer.playerBottomPlayPause.setImageResource(R.drawable.playicon)
        }


    }

    private fun initializeMediaController(songs: List<Song>, currentSong: Song) {
        lifecycleScope.launch {
            if (mediaController == null) {
                val sessionToken = SessionToken(
                    this@MainActivity,
                    ComponentName(this@MainActivity, MusicPlaybackService::class.java)
                )
                mediaController = MediaController.Builder(this@MainActivity, sessionToken)
                    .buildAsync().await()

                setMediaControllerListerns()
            }



            // Only set media items if there's no current media item (i.e. fresh session)
            //reopen from notification bar
            if (mediaController?.currentMediaItem == null) {
                val mediaItems = songs.map { song ->
                    MediaItem.Builder()
                        .setUri(song.data)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artist)
                                .setAlbumArtist(song.data)
                                .build()
                        )
                        .build()
                }

                mediaController?.setMediaItems(
                    mediaItems,
                    songs.indexOf(currentSong),
                    0L
                )
                mediaController?.prepare()
            }

            setRepeatModeUi(mediaController!!.repeatMode)
            updateSong(currentSong)
            setBottomPlayer()

        }
    }

    private fun setRepeatModeUi(currentRepeatMode: Int) {
        when (currentRepeatMode) {
            Player.REPEAT_MODE_ALL -> {
                binding.bottomPlayerContainer.playerBottomModeBtn.setImageResource(R.drawable.repeatall)
                repeatMode=RepeatMode.ALL
            }
            Player.REPEAT_MODE_ONE -> {
                binding.bottomPlayerContainer.playerBottomModeBtn.setImageResource(R.drawable.repeatone)
                repeatMode=RepeatMode.ONE
            }
            Player.REPEAT_MODE_OFF-> {
                binding.bottomPlayerContainer.playerBottomModeBtn.setImageResource(R.drawable.repeatoff)
                repeatMode=RepeatMode.OFF
            }
        }
    }


    private fun checkPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                binding.noPermisionContent.visibility=View.GONE
                binding.fragmentContainerView.visibility=View.VISIBLE
                viewModel.loadSongs()

            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }


    private fun observeSongs() {
        lifecycleScope.launchWhenStarted {
            viewModel.songs.collect { songs ->
                currentSongs = songs
                if (songs.isNotEmpty()) {
                    loadLastPlayedSong()
                    currentSong?.let {
                        if (mediaController==null){
                            initializeMediaController(songs, it)
                            binding.bottomPlayerContainer.bottomPlayerContent.visibility = View.VISIBLE
                        }

                    }
                }
            }
        }
    }

    fun songplay(songs: List<Song>, newSong: Song) {
        if (mediaController == null) {
            Log.d("MainActivity3","null")
            initializeMediaController(songs,newSong)
        } // Safety check, should already be initialized


        if (mediaController?.isPlaying == true
            && mediaController?.currentMediaItem?.mediaMetadata?.albumArtist.toString().equals(newSong.data)
            && songs.equals(currentSongs)){
            val action=NavigationgraphDirections.actionGlobalSongDetailFragment(newSong)
            navController.navigate(action)
            Log.d("MainActivity3","already playing")
            return
        }

        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setUri(song.data)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumArtist(song.data)
                        .build()
                )
                .build()
        }
        Log.d("MainActivity3","media item")

        mediaController?.apply {

            setMediaItems(mediaItems, songs.indexOf(newSong), 0L)
            prepare()
            play()
            Log.d("MainActivity3","apply")
        }


        viewModel.addSongToRecently(newSong.data)
        updateSong(newSong)
        currentSongs=songs
    }

    private  fun setMediaControllerListerns() {
        mediaController?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.mediaMetadata?.let { metadata ->

                    updateSong(Song(
                        metadata.title.toString(),
                        metadata.artist.toString(),
                        metadata.albumArtist.toString()
                    )
                    )

                }
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    binding.bottomPlayerContainer.playerBottomPlayPause.setImageResource(R.drawable.pause)
                } else {
                    binding.bottomPlayerContainer.playerBottomPlayPause.setImageResource(R.drawable.playicon)
                }
            }

            override fun onRepeatModeChanged(currentMode: Int) {
                super.onRepeatModeChanged(currentMode)
                setRepeatModeUi(currentMode)
            }
        })

    }

    fun updateSong(song:Song){
        binding.bottomPlayerContainer.playerBottomSongTitle.text = song.title
        binding.bottomPlayerContainer.playerBottomSongArtist.text = song.artist
        binding.bottomPlayerContainer.playerBottomSongTitle.isSelected = true
        binding.bottomPlayerContainer.playerBottomImage.glideSong(song)


        saveLastPlayedSong(song)
        loadLastPlayedSong()
    }

    fun saveLastPlayedSong(song: Song) {
        val prefs = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val json = JSONObject().apply {
            put("title", song.title)
            put("artist", song.artist)
            put("data", song.data)
        }
        prefs.edit().putString(LAST_SONG_KEY, json.toString()).apply()
    }

    fun loadLastPlayedSong(){

        val prefs = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val jsonStr = prefs.getString(LAST_SONG_KEY, null)
        if (jsonStr == null) {
            binding.bottomPlayerContainer.bottomPlayerContent.visibility=View.GONE
            currentSong= null
            return
        }
        val json = JSONObject(jsonStr)
        if(json.getString("title").isEmpty()){
            binding.bottomPlayerContainer.bottomPlayerContent.visibility=View.GONE
            currentSong= null
            return
        }

        currentSong=Song(
            title = json.getString("title"),
            artist = json.getString("artist"),
            data = json.getString("data")
        )
    }
    private enum class RepeatMode {
        OFF, ALL, ONE
    }


}
