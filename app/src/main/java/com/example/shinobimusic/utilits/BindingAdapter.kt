package com.example.shinobimusic.utilits

import android.media.MediaMetadataRetriever
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.shinobimusic.R
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@BindingAdapter("glideSong")
fun ImageView.glideSong(song: Song){
    this.setImageResource(R.drawable.songimage)

    val imageview=this
    // Use a coroutine to avoid blocking the UI thread
    CoroutineScope(Dispatchers.IO).launch {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(song.data)
            val art = retriever.embeddedPicture
            if (art != null) {
                withContext(Dispatchers.Main) {
                    Glide.with(imageview)
                        .asBitmap()
                        .load(art)
                        .placeholder(R.drawable.songimage)
                        .into(imageview)
                }
            }
        } catch (_: Exception) {
            // silently fail
        } finally {
            retriever.release()
        }
    }

}






@BindingAdapter("glideplaylist")
fun ImageView.glideplaylist(playlist: Playlist){
    val imageView=this
    if (playlist.songPaths.isNotEmpty()){
        CoroutineScope(Dispatchers.IO).launch {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(playlist.songPaths[0])
                val art = retriever.embeddedPicture
                if (art != null) {
                    withContext(Dispatchers.Main) {
                        Glide.with(imageView)
                            .asBitmap()
                            .load(art)
                            .placeholder(R.drawable.songimage)
                            .into(imageView)
                    }
                }
            } catch (_: Exception) {
                // silently fail
            } finally {
                retriever.release()
            }
        }
    }
}