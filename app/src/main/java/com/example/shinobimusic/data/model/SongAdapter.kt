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
import com.example.shinobimusic.databinding.SongItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongAdapter(
    private val onClick: (Song) -> Unit,
    private val onAddToPlaylist: (Song) -> Unit,
) : ListAdapter<Song,SongAdapter.SongViewHolder>(SongDiffCallback()) {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {

        val binding = SongItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song)
        holder.itemView.setOnClickListener { onClick(song) }
    }


    inner class SongViewHolder(private val binding: SongItemBinding) : RecyclerView.ViewHolder(binding.root) {


        fun bind(song: Song) {

            binding.song=song

            binding.songOptionsBtn.setOnClickListener {
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

                val anchor = it

                // Get screen location of the anchor (button)
                val location = IntArray(2)
                anchor.getLocationOnScreen(location)
                val anchorX = location[0]
                val anchorY = location[1]

                // Get height of screen
                val displayMetrics = anchor.context.resources.displayMetrics
                val screenHeight = displayMetrics.heightPixels

                // Measure the popup height
                popupView.measure(
                    View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED
                )
                val popupHeight = popupView.measuredHeight

                // Decide where to show: below or above the anchor
                val showAbove = (anchorY + anchor.height + popupHeight) > screenHeight

                if (showAbove) {
                    // Show above the anchor
                    popupWindow.showAtLocation(
                        anchor,
                        android.view.Gravity.NO_GRAVITY,
                        anchorX,
                        anchorY - popupHeight
                    )
                } else {
                    // Show below (same as showAsDropDown)
                    popupWindow.showAsDropDown(anchor, -100, 0)
                }

                // Handle menu item clicks
                popupView.findViewById<TextView>(R.id.menu_play).setOnClickListener {
                    onClick(song)
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