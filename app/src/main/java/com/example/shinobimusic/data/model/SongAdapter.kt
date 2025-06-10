package com.example.shinobimusic.data.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shinobimusic.R

class SongAdapter(
    private val onClick: (Song) -> Unit
) : ListAdapter<Song,SongAdapter.SongViewHolder>(SongDiffCallback()) {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song)
    }


    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.song_title)
        val artist = view.findViewById<TextView>(R.id.artist_name_song_item)
        val albumArt = view.findViewById<ImageView>(R.id.song_image)

        fun bind(song: Song) {
            title.text = song.title
            artist.text = song.artist
            Glide.with(itemView).load(song.albumArtBase64).into(albumArt)

            itemView.setOnClickListener { onClick(song) }
        }
    }

}


class SongDiffCallback : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem.data == newItem.data
    }

    override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
        return oldItem == newItem
    }

}