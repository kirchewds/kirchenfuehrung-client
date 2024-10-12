package de.kirchewds.kirchenfuehrung.client.playback

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import de.kirchewds.kirchenfuehrung.client.data.ToursRepository
import okhttp3.OkHttpClient

@UnstableApi
interface WithDataSources {
    val toursRepository: ToursRepository
    val playerCache: SimpleCache
    val context: Context
    val httpClient: OkHttpClient

    fun createDefaultDataSource() = OkHttpDataSource.Factory(httpClient)
    fun createCacheDataSource(mut: Boolean): CacheDataSource.Factory =
        CacheDataSource.Factory()
            .setCache(playerCache)
            .apply { if (!mut) setCacheWriteDataSinkFactory(null) }
            .setUpstreamDataSourceFactory(createDefaultDataSource())
}