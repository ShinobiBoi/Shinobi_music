package com.example.shinobimusic.di.module

import android.content.Context
import androidx.room.Room
import com.example.shinobimusic.data.local.AppDatabase
import com.example.shinobimusic.data.local.PlayListDao
import com.example.shinobimusic.data.local.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "shinobi_music_db"
        ).build()
    }

    @Provides
    fun provideSongDao(database: AppDatabase): SongDao = database.songDao()

    @Provides
    fun providePlayListDao(database: AppDatabase): PlayListDao = database.playlistDao()
}
