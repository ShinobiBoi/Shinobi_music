package com.example.shinobimusic.ui.playlist

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.data.model.SongAdapter
import com.example.shinobimusic.databinding.FragmentAllPlaylistsBinding
import com.example.shinobimusic.ui.songs.SongsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllPlaylistsFragment : Fragment() {

    private val viewModel: SongsViewModel by viewModels()
    private var currentPlaylists: List<Playlist> = emptyList()
    private lateinit var playListAdapter: PlayListAdapter
    private lateinit var binding: FragmentAllPlaylistsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAllPlaylistsBinding.inflate(inflater, container, false)

        // Observe playlists
        viewModel.playlists.observe(viewLifecycleOwner) {
            currentPlaylists = it
            setupViews()
            setupRecyclerView()
        }

        binding.addNewPlaylistBtn.setOnClickListener{
            showAddNewPlaylistDialog()
        }

        binding.addNewPlaylist.setOnClickListener(){
            showAddNewPlaylistDialog()
        }

        return binding.root
    }

    private fun setupViews(){
        if (currentPlaylists.isEmpty()) {
            binding.noPlaylistContent.visibility = View.VISIBLE
            binding.allplaylistContent.visibility = View.GONE
        } else {
            binding.noPlaylistContent.visibility = View.GONE
            binding.allplaylistContent.visibility = View.VISIBLE
        }

    }

    private fun setupRecyclerView() {

        playListAdapter = PlayListAdapter{
            navigateToPlaylist(it)
        }
        binding.playlistRecycleview.adapter = playListAdapter
        binding.playlistRecycleview.layoutManager= LinearLayoutManager(requireContext())
        playListAdapter.submitList(currentPlaylists)


    }

    private fun navigateToPlaylist(playlist: Playlist) {

        val action = AllPlaylistsFragmentDirections.actionAllPlaylistsFragmentToPlayListFragment(playlist)
        findNavController().navigate(action)

    }


    private fun showAddNewPlaylistDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Enter playlist name"
            setPadding(32, 24, 32, 24)
        }

        // Create a container layout to apply margin around the EditText
        val container = FrameLayout(requireContext()).apply {
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                // margin around the EditText inside the dialog
                leftMargin = 48
                topMargin = 24
                rightMargin = 48
            }
            addView(input, params)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Playlist")
            .setView(container)
            .setPositiveButton("Add") { _, _ ->
                val playlistName = input.text.toString().trim()
                if (playlistName.isNotEmpty()) {
                    viewModel.createPlaylist(playlistName)
                } else {
                    Toast.makeText(requireContext(), "Playlist name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }




}