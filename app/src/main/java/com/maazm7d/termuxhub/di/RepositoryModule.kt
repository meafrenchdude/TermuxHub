package com.maazm7d.termuxhub.di

import android.content.Context
import com.maazm7d.termuxhub.data.local.ToolDao
import com.maazm7d.termuxhub.data.local.HallOfFameDao
import com.maazm7d.termuxhub.data.remote.MetadataClient
import com.maazm7d.termuxhub.data.repository.ToolRepository
import com.maazm7d.termuxhub.data.repository.ToolRepositoryImpl
import com.maazm7d.termuxhub.data.repository.HallOfFameRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideToolRepository(
        dao: ToolDao,
        metadataClient: MetadataClient,
        context: Context
    ): ToolRepository = ToolRepositoryImpl(
        toolDao = dao,
        metadataClient = metadataClient,
        appContext = context
    )

    @Provides
    @Singleton
    fun provideHallOfFameRepository(
        metadataClient: MetadataClient,
        dao: HallOfFameDao
    ): HallOfFameRepository = HallOfFameRepository(
        metadataClient = metadataClient,
        dao = dao
    )
}
