package com.example.shinobimusic.data.model

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shinobimusic.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongAdapter(
    private val onClick: (Song) -> Unit,
    private val onAddToPlaylist: (Song) -> Unit
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
        val optionsBtn: ImageButton = itemView.findViewById(R.id.song_options_btn)


        fun bind(song: Song) {
            title.text = song.title
            artist.text = song.artist
            // Load placeholder first
            albumArt.setImageResource(R.drawable.songimage)

            // Use a coroutine to avoid blocking the UI thread
            CoroutineScope(Dispatchers.IO).launch {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(song.data)
                    val art = retriever.embeddedPicture
                    if (art != null) {
                        withContext(Dispatchers.Main) {
                            Glide.with(itemView.context)
                                .asBitmap()
                                .load(art)
                                .placeholder(R.drawable.songimage)
                                .into(albumArt)
                        }
                    }
                } catch (_: Exception) {
                    // silently fail
                } finally {
                    retriever.release()
                }
            }
            optionsBtn.setOnClickListener {
                val popupView = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.custom_song_popup, null)

                val popupWindow = PopupWindow(
                    popupView,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    true
                )

                popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                popupWindow.elevation = 8f

                // Optional: position below the button
                popupWindow.showAsDropDown(optionsBtn, -100, 0)

                // Handle menu item clicks
                popupView.findViewById<TextView>(R.id.menu_play).setOnClickListener {
                    // Handle play
                    popupWindow.dismiss()
                }

                popupView.findViewById<TextView>(R.id.menu_add_to_playlist).setOnClickListener {
                    // Handle add to playlist
                    onAddToPlaylist(song)
                    popupWindow.dismiss()
                }

                popupView.findViewById<TextView>(R.id.menu_delete).setOnClickListener {
                    // Handle delete
                    popupWindow.dismiss()
                }
            }
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