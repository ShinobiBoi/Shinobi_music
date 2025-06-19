package com.example.shinobimusic.ui.artist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.domain.repo.ArtistRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val repository: ArtistRepo
) : ViewModel() {


    val playlists: LiveData<List<Playlist>> = repository.getAllPlaylists()


    fun createArtistPlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            val allSongs = repository.getSongs()

            // Group songs by artist
            val artistToSongsMap = allSongs.groupBy { it.artist.lowercase() }

            for ((artist, songs) in artistToSongsMap) {
                val name = "$artist Artist"
                val songPaths = songs.map { it.data }

                val existing = repository.getPlaylistByName(name)
                if (existing != null) {
                    // Update existing
                    repository.updatePlaylist(existing.copy(songPaths = songPaths))
                } else {
                    // Insert new
                    repository.createPlaylist(Playlist(name = name, songPaths = songPaths))
                }
            }
        }
    }

}