package com.example.shinobimusic.ui.songs

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shinobimusic.MainActivity
import com.example.shinobimusic.R
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.data.model.SongAdapter
import com.example.shinobimusic.databinding.FragmentSongsBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SongsFragment : Fragment() {

    private val viewModel: SongsViewModel by viewModels()

    private lateinit var binding:FragmentSongsBinding



    private lateinit var songAdapter: SongAdapter
    private var currentPlaylists: List<Playlist> = emptyList()
    private var currentSongs: List<Song> = emptyList()

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
        binding=FragmentSongsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        checkPermission()
        observeSongs()
        observeLoading()
        setUpButtons()
        setUpSearchView()

        // Observe playlists
        viewModel.playlists.observe(viewLifecycleOwner) {
            currentPlaylists = it
        }
    }

    private fun setUpButtons() {
        binding.backBtnAllSongs.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.playBtn.setOnClickListener{
            (activity as MainActivity).songplay(currentSongs,currentSongs.first())
        }
        binding.shuffleBtn.setOnClickListener{
            (activity as MainActivity).songplay(currentSongs.shuffled(),currentSongs.first())
        }

        binding.scanBtnAllSongs.setOnClickListener{
            viewModel.scanSongs()
        }
    }

    private fun setUpSearchView() {

        binding.searchViewSongs.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }


            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = if (newText.isNullOrBlank()) {
                    currentSongs
                } else {
                    currentSongs.filter {
                        it.title.contains(newText, ignoreCase = true) ||
                                it.artist.contains(newText, ignoreCase = true)
                    }
                }
                songAdapter.submitList(filteredList)
                return true
            }
        })

        val searchText = binding.searchViewSongs.findViewById<android.widget.EditText>(
            androidx.appcompat.R.id.search_src_text
        )
        searchText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black)) // or your desired color
        searchText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.lightgrey))
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            onClick = { currentSong ->
                (activity as MainActivity).songplay(currentSongs,currentSong)
            }
            ,
            onAddToPlaylist = { song ->
                showAddToPlaylistDialog(song)
            }
        )
        binding.songsRv.layoutManager = LinearLayoutManager(requireContext())
        binding.songsRv.adapter = songAdapter
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
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.songsRv.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }
    }

        private fun observeSongs() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.songs.collect { songs ->
                currentSongs=songs
                songAdapter.submitList(currentSongs)
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
