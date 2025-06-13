package com.example.shinobimusic.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shinobimusic.R
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.data.model.SongAdapter
import com.example.shinobimusic.databinding.FragmentPlayListBinding
import com.example.shinobimusic.ui.songs.SongsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayListFragment : Fragment() {
    private val args: PlayListFragmentArgs by navArgs()
    private val viewModel: SongsViewModel by viewModels()

    private lateinit var songAdapter: SongAdapter
    private var currentPlaylists: List<Playlist> = emptyList()
    private lateinit var binding: FragmentPlayListBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPlayListBinding.inflate(inflater, container, false)
        binding.playlist=args.playlist

        setupRecyclerView()
        checkPermission()
        observeSongs()


        // Observe playlists
        viewModel.playlists.observe(viewLifecycleOwner) {
            currentPlaylists = it
        }



        return binding.root
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.loadSongsByPaths(args.playlist.songPaths)
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
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
        binding.playlistSongsRv.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistSongsRv.adapter = songAdapter
    }

    private fun checkPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_AUDIO
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.loadSongsByPaths(args.playlist.songPaths)
            }
            else -> {
                requestPermissionLauncher.launch(permission)
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