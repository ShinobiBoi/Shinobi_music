package com.example.shinobimusic.ui.home

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shinobimusic.MainActivity
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.utilits.SongAdapter
import com.example.shinobimusic.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay


@AndroidEntryPoint
class HomeFragment : Fragment() {


    val viewModel: HomeViewModel by viewModels()
    private var currentPlaylists: List<Playlist> = emptyList()
    private var currentSongs: List<Song> = emptyList()
    private lateinit var songAdapter: SongAdapter

    private lateinit var binding: FragmentHomeBinding



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.playlistOption.setOnClickListener(){
            val action = HomeFragmentDirections.actionHomeFragmentToAllPlaylistsFragment()
            findNavController().navigate(action)

        }

        binding.artistOption.setOnClickListener(){
            val action = HomeFragmentDirections.actionHomeFragmentToArtistFragment()
            findNavController().navigate(action)

        }

        binding.songsOption.setOnClickListener(){
            val action = HomeFragmentDirections.actionHomeFragmentToSongsFragment2()
            findNavController().navigate(action)

        }

        viewModel.playlists.observe(viewLifecycleOwner){
            currentPlaylists = it
            viewModel.loadSongsByPaths(it[1].songPaths)
        }

        setupRecyclerView()
        observeSongs()

        binding.recentRecyclerView





        return binding.root
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter(
            onClick =  { song ->
                (activity as MainActivity).songplay(currentSongs,song)

            }
            ,
            onAddToPlaylist = { song ->
                showAddToPlaylistDialog(song)
            }
        )
        binding.recentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recentRecyclerView.adapter = songAdapter
    }



    private fun observeSongs() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.songs.collect { songs ->
                currentSongs=songs
                songAdapter.submitList(songs)
                delay(400)
                binding.recentRecyclerView.smoothScrollToPosition(0)
            }
        }
    }


    private fun showAddToPlaylistDialog(song: Song) {

        val allPlaylists =currentPlaylists.filter {
            it.name != "Favorites" && it.name != "Recently Played" && !it.name.contains("Artist")
        }
        if (allPlaylists.isEmpty()) {
            return
        }
        val playlistNames = allPlaylists.map { it.name }.toTypedArray()
        val checkedItems = BooleanArray(allPlaylists.size) { index ->
            // ✅ Pre-check if song path already exists in this playlist
            allPlaylists[index].songPaths.contains(song.data)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add '${song.title}' to Playlist(s)")
            .setMultiChoiceItems(playlistNames, checkedItems) { _, index, isChecked ->
                checkedItems[index] = isChecked
            }
            .setPositiveButton("Add") { _, _ ->
                for (i in checkedItems.indices) {
                    if (checkedItems[i]) {
                        viewModel.addSongToPlaylist(allPlaylists[i].id, song.data)
                    } else if (allPlaylists[i].songPaths.contains(song.data)) {
                        // Optional: remove from playlist if it was unchecked
                        viewModel.removeSongFromPlaylist(allPlaylists[i].id, song.data)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



}