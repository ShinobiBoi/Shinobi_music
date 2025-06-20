package com.example.shinobimusic.ui.songs

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.domain.repo.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val repository: SongRepository
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    val playlists: LiveData<List<Playlist>> = repository.getAllPlaylists()

    fun loadSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            _songs.value = repository.getSongs()
            _isLoading.value = false
        }
    }
    fun loadSongsByPaths(songPaths: List<String>){
        viewModelScope.launch {
            _isLoading.value = true
            _songs.value = repository.getSongsByPaths(songPaths)
            _isLoading.value = false
        }
    }

    fun scanSongs(){
        viewModelScope.launch {
            _isLoading.value = true
            _songs.value = repository.scanSongs()
            _isLoading.value = false
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(Playlist(name = name, songPaths = emptyList()))
        }
    }

    fun addSongToPlaylist(playlistId: Int, songPath: String) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songPath)
        }
    }

    fun removeSongFromPlaylist(playlistId: Int, songPath: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songPath)
        }
    }
    fun addSongToRecently(songPath: String){
        viewModelScope.launch {
            repository.addSongToRecently(songPath)
        }
    }

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

    fun deletePlaylist(playlistId: Int) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    suspend fun isSongInPlaylist(playlistId: Int, songPath: String): Boolean {
        return repository.isSongInPlaylist(playlistId, songPath)
    }
}