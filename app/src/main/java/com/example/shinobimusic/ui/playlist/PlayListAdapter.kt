package com.example.shinobimusic.ui.playlist


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.databinding.PlaylistItemBinding

class PlayListAdapter(
    private val onClick: (Playlist) -> Unit,
):ListAdapter<Playlist, PlayListAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {


    inner class PlaylistViewHolder(private val binding: PlaylistItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.playlist=playlist
            if (playlist.songPaths.isNotEmpty())
                binding.playlistMusicnote.visibility=View.GONE

            itemView.setOnClickListener { onClick(playlist) }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding=PlaylistItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.bind(playlist)
    }

}

class PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
    override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
        return oldItem == newItem
    }

}