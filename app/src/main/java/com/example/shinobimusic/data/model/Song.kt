package com.example.shinobimusic.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Song(
    val title: String,
    val artist: String,
    @PrimaryKey
    val data: String,
): Serializable
