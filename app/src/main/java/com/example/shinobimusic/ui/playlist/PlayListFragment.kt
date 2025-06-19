package com.example.shinobimusic.ui.playlist

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shinobimusic.MainActivity
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.databinding.FragmentPlayListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlayListFragment : Fragment() {
    private val args: PlayListFragmentArgs by navArgs()
    private val viewModel: PlayListViewModel by viewModels()


    private lateinit var binding: FragmentPlayListBinding
    private lateinit var songAdapter: PlayListSongsAdapter
    private lateinit var currentPlaylist: Playlist
    private var allPlaylists: List<Playlist> = emptyList()
    private var currentSongs: List<Song> = emptyList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPlayListBinding.inflate(inflater, container, false)


        currentPlaylist=args.playlist
        binding.playlist=currentPlaylist


        setupRecyclerView()
        observeSongs()


        // Observe playlists
        viewModel.playlists.observe(viewLifecycleOwner) {
            allPlaylists = it.filter { it.name != "Favorites" && it.name != "Recently Played" && !it.name.contains("Artist")  }
            currentPlaylist=it.first { it.id == currentPlaylist.id }
            viewModel.loadSongsByPaths(currentPlaylist.songPaths)
        }

        binding.backBtnPlaylist.setOnClickListener{
            findNavController().popBackStack()
        }

        binding.playlistPlayBtn.setOnClickListener{
            (activity as MainActivity).songplay(currentSongs,currentSongs.first())
        }
        binding.playlistShuffleBtn.setOnClickListener{
            (activity as MainActivity).songplay(currentSongs.shuffled(),currentSongs[(currentSongs.indices).random()])
        }



        return binding.root
    }



    private fun setupRecyclerView() {
        songAdapter = PlayListSongsAdapter(
            onClick =  { song ->
                (activity as MainActivity).songplay(currentSongs,song)

            }
            ,
            onAddToPlaylist = { song ->
                showAddToPlaylistDialog(song)
            },
            onRemove = { song ->
                viewModel.removeSongFromPlaylist(currentPlaylist.id, song.data)
            }
        )
        binding.playlistSongsRv.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistSongsRv.adapter = songAdapter
    }



    private fun observeSongs() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.songs.collect { songs ->
                currentSongs=songs
                songAdapter.submitList(songs)
            }
        }
    }


    private fun showAddToPlaylistDialog(song: Song) {
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