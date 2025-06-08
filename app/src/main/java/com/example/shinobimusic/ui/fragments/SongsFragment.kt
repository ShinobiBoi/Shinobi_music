package com.example.shinobimusic.ui.fragments

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.shinobimusic.R
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.data.model.SongAdapter



class SongsFragment : Fragment() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private var songList = mutableListOf<Song>()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                loadSongs()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_songs, container, false)

        recyclerView = view.findViewById(R.id.songs_rv)
        checkPermission()
        return view
    }

    private fun checkPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                loadSongs()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun loadSongs() {
        songList = getAudioFiles()
        songAdapter = SongAdapter(songList) { song ->
/*            val intent = Intent(requireContext(), DetailSongActivity::class.java).apply {
                putExtra("songPath", song.data)
                putExtra("title", song.title)
                putExtra("artist", song.artist)
                putExtra("albumArt", song.albumArt)
            }
            startActivity(intent)*/
        }
        recyclerView.adapter = songAdapter
    }

    private fun getAudioFiles(): MutableList<Song> {
        val songs = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        val cursor = requireContext().contentResolver.query(uri, projection, selection, null, sortOrder)

        cursor?.use {
            val titleIndex = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistIndex = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val dataIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)
            val albumIdIndex = it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

            val retriever = MediaMetadataRetriever()

            while (it.moveToNext()) {
                val title = it.getString(titleIndex)
                val artist = it.getString(artistIndex)
                val data = it.getString(dataIndex)

                // ❌ Skip certain folders
                val excludedPaths = listOf(
                    "/WhatsApp/",                // WhatsApp
                    "/.thumbnails/",             // Hidden thumbnails
                    "/Notifications/",           // Notification tones
                    "/Ringtones/",               // Ringtones
                    "/Alarms/",                  // Alarm tones
                    "/CallRecorder/",            // Call recordings
                    "/Download/voice notes"      // Voice note folder (example)
                )

                if (excludedPaths.any { data.contains(it, ignoreCase = true) }) {
                    continue
                }

                var albumArtUri: String? = null

                try {
                    retriever.setDataSource(data)
                    val art = retriever.embeddedPicture
                    if (art != null) {
                        val base64Art = Base64.encodeToString(art, Base64.DEFAULT)
                        albumArtUri = "data:image/jpeg;base64,$base64Art"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                songs.add(Song(title, artist, data, albumArtUri ?: ""))
            }

            retriever.release()
        }

        return songs
    }


}