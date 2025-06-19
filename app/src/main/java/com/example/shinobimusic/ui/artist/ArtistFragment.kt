package com.example.shinobimusic.ui.artist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.databinding.FragmentArtistBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArtistFragment : Fragment() {

    private lateinit var binding:FragmentArtistBinding
    val viewModel: ArtistViewModel by viewModels()
    private lateinit var adapter: ArtistAdapter

    private lateinit var currentPlaylists:List<Playlist>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding=FragmentArtistBinding.inflate(inflater,container,false)

        binding.scanBtnArtist.setOnClickListener{
            viewModel.createArtistPlaylists()
        }

        viewModel.playlists.observe(viewLifecycleOwner){
            currentPlaylists=it.filter { it.name.contains("Artist") }.sortedByDescending{  it.songPaths.size }
            if (currentPlaylists.isEmpty()){
                binding.artistRecycleview.visibility=View.GONE
                binding.noArtistListText.visibility=View.VISIBLE
            }else{
                binding.artistRecycleview.visibility=View.VISIBLE
                binding.noArtistListText.visibility=View.GONE
            }

            adapter.submitList(currentPlaylists)
        }

        binding.backBtnArtist.setOnClickListener{
            findNavController().popBackStack()
        }


        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {

        adapter = ArtistAdapter{
            navigateToPlaylist(it)
        }
        binding.artistRecycleview.adapter=adapter
        binding.artistRecycleview.layoutManager= LinearLayoutManager(requireContext())

    }

    private fun navigateToPlaylist(playlist: Playlist) {
        val action = ArtistFragmentDirections.actionArtistFragmentToPlayListFragment2(playlist)
        findNavController().navigate(action)

    }


}