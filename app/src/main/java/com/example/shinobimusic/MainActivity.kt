package com.example.shinobimusic


import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log

import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import com.example.shinobimusic.data.model.glideSong
import com.example.shinobimusic.databinding.ActivityMainBinding
import com.example.shinobimusic.ui.MusicPlaybackService
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
    private  var song: Song?=null

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


    override fun onStart() {
        super.onStart()
        reconnectToMediaController()
    }

       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           binding = ActivityMainBinding.inflate(layoutInflater)
           setContentView(binding.root)


           val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
           navController = navHostFragment.navController

          iniztialcheck()

           binding.noPermisionBtn.setOnClickListener {
               checkPermission()
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
        song?.let { updateSong(it) }

        binding.bottomPlayerContainer.playerBottomPlayPause.setOnClickListener {

           if (mediaController == null || mediaController?.mediaItemCount == 0 || mediaController?.currentMediaItem == null){
               Log.d("MainActivity3", "MediaController not initialized or empty")
               songplay(currentSongs,song!!)
           }




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


    }

    private fun reconnectToMediaController() {
        lifecycleScope.launch {
            if (mediaController == null) {
                val sessionToken = SessionToken(
                    this@MainActivity,
                    ComponentName(this@MainActivity, MusicPlaybackService::class.java)
                )
                mediaController =
                    MediaController.Builder(this@MainActivity, sessionToken).buildAsync().await()

                setMediaControllerListerns()

                when ( mediaController?.repeatMode){
                    Player.REPEAT_MODE_ALL->{
                        binding.bottomPlayerContainer.playerBottomModeBtn.setImageResource(R.drawable.repeatall)
                        repeatMode=RepeatMode.ALL
                    }
                    Player.REPEAT_MODE_ONE->{
                        binding.bottomPlayerContainer.playerBottomModeBtn.setImageResource(R.drawable.repeatone)
                        repeatMode=RepeatMode.ONE
                    }
                    Player.REPEAT_MODE_OFF->{
                        binding.bottomPlayerContainer.playerBottomModeBtn.setImageResource(R.drawable.repeatoff)
                        repeatMode=RepeatMode.OFF
                    }
                }
                if (mediaController?.isPlaying == true) {
                    // Media is currently playing
                    binding.bottomPlayerContainer.playerBottomPlayPause.setImageResource(R.drawable.pause)
                } else {
                    // Media is paused or stopped
                    binding.bottomPlayerContainer.playerBottomPlayPause.setImageResource(R.drawable.playicon)
                }

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
                currentSongs=songs
            }
        }
    }

    fun songplay(songs:List<Song>,currentSong:Song){

        val mediaItems = songs.map {song->
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

        lifecycleScope.launch {
            if (mediaController == null) {
                val sessionToken = SessionToken(
                    this@MainActivity,
                    ComponentName(this@MainActivity, MusicPlaybackService::class.java)
                )
                mediaController =
                    MediaController.Builder(this@MainActivity, sessionToken).buildAsync()
                        .await()
            }

            setMediaControllerListerns()

            mediaController?.apply {
                setMediaItems(
                    mediaItems,
                    songs.indexOf(currentSong),
                    0L
                ) // start from clicked song
                prepare()
                play()
            }
        }
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
        })

    }

    fun updateSong(song:Song){
        binding.bottomPlayerContainer.playerBottomSongTitle.text = song.title
        binding.bottomPlayerContainer.playerBottomSongArtist.text = song.artist
        binding.bottomPlayerContainer.playerBottomSongTitle.isSelected = true
        binding.bottomPlayerContainer.playerBottomImage.glideSong(song)

        binding.bottomPlayerContainer.bottomPlayerContent.visibility=View.VISIBLE

        saveLastPlayedSong(song)
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
            song= null
            return
        }
        val json = JSONObject(jsonStr)
        if(json.getString("title").isEmpty()){
            binding.bottomPlayerContainer.bottomPlayerContent.visibility=View.GONE
            song= null
            return
        }

        song=Song(
            title = json.getString("title"),
            artist = json.getString("artist"),
            data = json.getString("data")
        )
    }
    private enum class RepeatMode {
        OFF, ALL, ONE
    }


}
