package com.example.shinobimusic.ui.artist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.databinding.ArtistItemBinding

class ArtistAdapter(
    private val onClick: (Playlist) -> Unit,
): ListAdapter<Playlist, ArtistAdapter.ArtistViewHolder>(ArtistCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding=ArtistItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.bind(playlist)
    }



    inner class ArtistViewHolder(private val binding: ArtistItemBinding) :RecyclerView.ViewHolder(binding.root){
        fun bind(playlist: Playlist) {
            binding.playlist=playlist
            itemView.setOnClickListener { onClick(playlist) }

        }
    }

}

private class ArtistCallback: DiffUtil.ItemCallback<Playlist>() {
    override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem == newItem
    }
}