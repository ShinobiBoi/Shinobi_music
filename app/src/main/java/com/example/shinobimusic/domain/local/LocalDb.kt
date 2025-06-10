package com.example.shinobimusic.domain.local

import com.example.shinobimusic.data.model.Song

interface LocalDb {
    suspend fun getSongs(): List<Song>
}