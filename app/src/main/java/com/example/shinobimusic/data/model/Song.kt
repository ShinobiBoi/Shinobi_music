package com.example.shinobimusic.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Song(
    val title: String,
    val artist: String,
    @PrimaryKey
    val data: String,
)
