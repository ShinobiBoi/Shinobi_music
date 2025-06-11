package com.example.shinobimusic.ui.songs

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shinobimusic.R
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.data.model.SongAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SongsFragment : Fragment() {

    private val viewModel: SongsViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter

    private lateinit var progressBar: ProgressBar

    private var currentPlaylists: List<Playlist> = emptyList()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.loadSongs()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.songs_rv)
        progressBar = view.findViewById(R.id.progress_bar)
        setupRecyclerView()
        checkPermission()
        observeSongs()
        observeLoading()

        // Observe playlists
        viewModel.playlists.observe(viewLifecycleOwner) {
            currentPlaylists = it
        }
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            onClick =  { song -> }
            ,
            onAddToPlaylist = { song ->
                showAddToPlaylistDialog(song)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = songAdapter
    }

    private fun checkPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.loadSongs()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun observeLoading() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.isLoading.collect { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }
    }

        private fun observeSongs() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.songs.collect { songs ->
                songAdapter.submitList(songs)
            }
        }
    }

    private fun showAddToPlaylistDialog(song: Song) {
        if (currentPlaylists.isEmpty()) {
            Toast.makeText(requireContext(), "No playlists found", Toast.LENGTH_SHORT).show()
            return
        }

        val playlistNames = currentPlaylists.map { it.name }.toTypedArray()
        val checkedItems = BooleanArray(currentPlaylists.size) { index ->
            // ✅ Pre-check if song path already exists in this playlist
            currentPlaylists[index].songPaths.contains(song.data)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add '${song.title}' to Playlist(s)")
            .setMultiChoiceItems(playlistNames, checkedItems) { _, index, isChecked ->
                checkedItems[index] = isChecked
            }
            .setPositiveButton("Add") { _, _ ->
                for (i in checkedItems.indices) {
                    if (checkedItems[i]) {
                        viewModel.addSongToPlaylist(currentPlaylists[i].id, song.data)
                    } else if (currentPlaylists[i].songPaths.contains(song.data)) {
                        // Optional: remove from playlist if it was unchecked
                        viewModel.removeSongFromPlaylist(currentPlaylists[i].id, song.data)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


}
