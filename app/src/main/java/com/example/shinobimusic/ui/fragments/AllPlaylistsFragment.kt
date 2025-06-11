package com.example.shinobimusic.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.shinobimusic.R
import com.example.shinobimusic.databinding.FragmentAllPlaylistsBinding
import com.example.shinobimusic.ui.songs.SongsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllPlaylistsFragment : Fragment() {

    private val viewModel: SongsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentAllPlaylistsBinding.inflate(inflater, container, false)




        binding.addNewPlaylistBtn.setOnClickListener{
            viewModel.createPlaylist("New Playlist")
            Log.d("SongsFragment", "New playlist created")


        }

        return binding.root
    }


}