package com.example.shinobimusic.di.bind

import com.example.shinobimusic.data.local.LocalDbImp
import com.example.shinobimusic.domain.local.LocalDb
import com.example.shinobimusic.domain.repo.SongRepository
import com.example.shinobimusic.ui.songs.SongRepositoryImp
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class BindModule {


    @Binds
    abstract fun bindSongRepository(songRepositoryImpl: SongRepositoryImp): SongRepository

    @Binds
    abstract fun bindLocalDb(localDbImp: LocalDbImp): LocalDb

}