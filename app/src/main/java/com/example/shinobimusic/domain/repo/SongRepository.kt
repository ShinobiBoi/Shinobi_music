package com.example.shinobimusic.domain.repo

import com.example.shinobimusic.data.model.Song

interface SongRepository {
    suspend fun getSongs(): List<Song>
}