package com.example.shinobimusic.di.bind

import com.example.shinobimusic.data.local.LocalDbImp
import com.example.shinobimusic.domain.local.LocalDb
import com.example.shinobimusic.domain.repo.AllPlaylistsRepo
import com.example.shinobimusic.domain.repo.ArtistRepo
import com.example.shinobimusic.domain.repo.HomeRepo
import com.example.shinobimusic.domain.repo.PlayListRepo
import com.example.shinobimusic.domain.repo.SongRepository
import com.example.shinobimusic.ui.allplaylist.AllPlaylistsRepoImp
import com.example.shinobimusic.ui.artist.ArtistRepoImp
import com.example.shinobimusic.ui.home.HomeRepoImp
import com.example.shinobimusic.ui.playlist.PlayListRepoImp
import com.example.shinobimusic.ui.songs.SongRepositoryImp
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class BindModule {

    @Binds
    abstract fun bindLocalDb(localDbImp: LocalDbImp): LocalDb

    @Binds
    abstract fun bindSongRepository(songRepositoryImpl: SongRepositoryImp): SongRepository


    @Binds
    abstract fun bindAllPlaylistsRepo(allPlaylistsRepoImp: AllPlaylistsRepoImp): AllPlaylistsRepo

    @Binds
    abstract fun bindPlayListRepo(playlistsRepoImp: PlayListRepoImp): PlayListRepo

    @Binds
    abstract fun bindHomeRepo(homeRepoImp: HomeRepoImp): HomeRepo

    @Binds
    abstract fun bindArtistRepo(artistRepoImp: ArtistRepoImp): ArtistRepo
}