package com.example.shinobimusic.ui.allplaylist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.domain.repo.AllPlaylistsRepo
import com.example.shinobimusic.domain.repo.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AllPlaylistsViewModel @Inject constructor(
    private val repository: AllPlaylistsRepo
) : ViewModel() {

    val playlists: LiveData<List<Playlist>> = repository.getAllPlaylists()



    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(Playlist(name = name, songPaths = emptyList()))
        }
    }


}