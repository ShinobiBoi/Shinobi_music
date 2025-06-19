package com.example.shinobimusic.ui.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.domain.repo.PlayListRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PlayListViewModel @Inject constructor(
    private val repository: PlayListRepo
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs



    val playlists: LiveData<List<Playlist>> = repository.getAllPlaylists()


    fun loadSongsByPaths(songPaths: List<String>){
        viewModelScope.launch {
            _songs.value = repository.getSongsByPaths(songPaths)
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


}