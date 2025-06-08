package com.example.shinobimusic.data.model

data class Song(
    val title: String,
    val artist: String,
    val data: String,
    val albumArtBase64: String? // embedded image in Base64 format
)
