package com.example.shinobimusic.ui.songdetail

import android.content.ComponentName
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.fragment.findNavController
import androidx.palette.graphics.Palette
import com.example.shinobimusic.R
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.data.model.glideSong
import com.example.shinobimusic.databinding.FragmentSongDetailBinding
import com.example.shinobimusic.ui.MusicPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongDetailFragment : Fragment() {

    private lateinit var binding: FragmentSongDetailBinding
    private val handler = Handler(Looper.getMainLooper())
    private var mediaController: MediaController? = null

    private val updateSeekbar = object : Runnable {
        override fun run() {
            mediaController?.let {
                binding.songDetailSeekbar.progress = it.currentPosition.toInt()
                binding.songDetailTimestamp.text = formatTime(it.currentPosition)
                if (it.duration > 0)
                    binding.songDetailDuration.text = formatTime(it.duration)
                handler.postDelayed(this, 100)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSongDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val token = SessionToken(requireContext(), ComponentName(requireContext(), MusicPlaybackService::class.java))

        CoroutineScope(Dispatchers.Main).launch {
            mediaController = withContext(Dispatchers.IO) {
                MediaController.Builder(requireContext(), token).buildAsync().await()
            }

            mediaController?.let { controller ->
                controller.addListener(playerListener)

                setupUIFromMediaItem(controller.currentMediaItem)
                setupListeners()

                binding.songDetailSeekbar.max = controller.duration.toInt()
                handler.post(updateSeekbar)
            }
        }
    }

    private fun setupUIFromMediaItem(mediaItem: MediaItem?) {
        val song = mediaItem?.mediaMetadata?.let {
            Song(
                title = it.title?.toString() ?: "Unknown",
                artist = it.artist?.toString() ?: "Unknown",
                data = it.albumArtist?.toString() ?: "Unknown"
            )
        }

        song?.let {
            binding.songDetailTitle.text = it.title
            binding.songDetailArtist.text = it.artist
            binding.songDetailTitle.isSelected = true
            binding.songDetailImage.glideSong(it)
            setBackgroundDominantColor(it.data)
        }

        updatePlayPauseIcon()
        updateRepeatModeIcon()
    }

    private fun setBackgroundDominantColor(songPath: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(songPath)
                val art = retriever.embeddedPicture
                if (art != null) {
                    val bitmap = BitmapFactory.decodeByteArray(art, 0, art.size)

                    withContext(Dispatchers.Main) {
                        Palette.from(bitmap).generate { palette ->
                            val dominantColor = palette?.getDominantColor(
                                ContextCompat.getColor(requireContext(), R.color.black)
                            )

                            dominantColor?.let { color ->
                                binding.songDetailContent.setBackgroundColor(color)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ColorExtract", "Error getting dominant color: ${e.message}")
            } finally {
                retriever.release()
            }
        }
    }

    private fun setupListeners() {
        binding.songDetailDownbtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.songDetailPlaypausebtn.setOnClickListener {
            mediaController?.let {
                if (it.isPlaying) it.pause() else it.play()
                updatePlayPauseIcon()
            }
        }

        binding.songDetailPreviousbtn.setOnClickListener {
            mediaController?.seekToPrevious()
        }

        binding.songDetailNextbtn.setOnClickListener {
            mediaController?.seekToNext()
        }

        binding.songDetailRepeatmode.setOnClickListener {
            mediaController?.let {
                val newMode = when (it.repeatMode) {
                    Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                    Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                    else -> Player.REPEAT_MODE_OFF
                }
                it.repeatMode = newMode
                updateRepeatModeIcon()
            }
        }

        binding.songDetailSeekbar.setOnSeekBarChangeListener(object :
            android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaController?.seekTo(progress.toLong())
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })
    }

    private fun updatePlayPauseIcon() {
        if (mediaController?.isPlaying == true) {
            binding.songDetailPlaypausebtn.setImageResource(R.drawable.pause)
        } else {
            binding.songDetailPlaypausebtn.setImageResource(R.drawable.playicon)
        }
    }

    private fun updateRepeatModeIcon() {
        val icon = when (mediaController?.repeatMode) {
            Player.REPEAT_MODE_ALL -> R.drawable.repeatall
            Player.REPEAT_MODE_ONE -> R.drawable.repeatone
            else -> R.drawable.repeatoff
        }
        binding.songDetailRepeatmode.setImageResource(icon)
    }

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            setupUIFromMediaItem(mediaItem)
        }
    }

    private fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateSeekbar)
        mediaController?.removeListener(playerListener)
        mediaController?.release()
    }
}
