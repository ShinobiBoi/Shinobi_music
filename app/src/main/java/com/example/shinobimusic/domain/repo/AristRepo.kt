package com.example.shinobimusic.domain.repo

import androidx.lifecycle.LiveData
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song

interface ArtistRepo {
    fun getAllPlaylists(): LiveData<List<Playlist>>
    suspend fun getSongs(): List<Song>
    suspend fun getPlaylistByName(name: String): Playlist?
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun createPlaylist(playlist: Playlist)
}
