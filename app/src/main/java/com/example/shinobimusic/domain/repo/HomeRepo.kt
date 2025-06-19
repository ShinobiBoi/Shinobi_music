package com.example.shinobimusic.domain.repo

import androidx.lifecycle.LiveData
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song

interface HomeRepo {
    fun getAllPlaylists(): LiveData<List<Playlist>>
    suspend fun getSongsByPaths(songPaths: List<String>): List<Song>
    suspend fun createPlaylist(playlist: Playlist)
    suspend fun addSongToPlaylist(playlistId: Int, songPath: String)
    suspend fun removeSongFromPlaylist(playlistId: Int, songPath: String)
}
