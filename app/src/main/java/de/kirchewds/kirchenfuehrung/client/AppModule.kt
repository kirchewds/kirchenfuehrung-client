package de.kirchewds.kirchenfuehrung.client

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.kirchewds.kirchenfuehrung.client.data.ToursRepository
import de.kirchewds.kirchenfuehrung.client.data.impl.NetworkToursRepository
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideRepository(@ApplicationContext context: Context): ToursRepository =
        NetworkToursRepository(ClientApplication.TOURS_JSON_URI.toHttpUrl())

    @Singleton
    @Provides
    @UnstableApi
    fun provideDatabaseProvider(@ApplicationContext context: Context): DatabaseProvider =
        StandaloneDatabaseProvider(context)

    @Singleton
    @Provides
    @UnstableApi
    fun providePlayerCache(@ApplicationContext context: Context, databaseProvider: DatabaseProvider): SimpleCache =
        SimpleCache(
            context.filesDir.resolve("exoplayer"),
            LeastRecentlyUsedCacheEvictor(1024 * 1024 * 1024L),
            databaseProvider
        )
}