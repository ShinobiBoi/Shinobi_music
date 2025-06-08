package com.example.shinobimusic.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.shinobimusic.R
import com.example.shinobimusic.data.model.OptionItem
import com.example.shinobimusic.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.playlistOption.setOnClickListener(){
            val action = HomeFragmentDirections.actionHomeFragmentToPlayListFragment()
            findNavController().navigate(action)

        }

        binding.songsOption.setOnClickListener(){
            val action = HomeFragmentDirections.actionHomeFragmentToSongsFragment2()
            findNavController().navigate(action)

        }





        return binding.root
    }


}