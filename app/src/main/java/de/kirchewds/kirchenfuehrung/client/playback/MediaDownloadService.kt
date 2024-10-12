package de.kirchewds.kirchenfuehrung.client.playback

import android.app.Notification
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.DatabaseProvider
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Scheduler
import androidx.media3.exoplayer.workmanager.WorkManagerScheduler
import dagger.hilt.android.AndroidEntryPoint
import de.kirchewds.kirchenfuehrung.client.data.ToursRepository
import okhttp3.OkHttpClient
import java.util.concurrent.Executor
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MediaDownloadService : DownloadService(0), WithDataSources {
    @Inject override lateinit var toursRepository: ToursRepository
    @Inject override lateinit var playerCache: SimpleCache
    @Inject override lateinit var httpClient: OkHttpClient
    @Inject lateinit var databaseProvider: DatabaseProvider
    override val context = this

    override fun getDownloadManager(): DownloadManager {
        val downloadExecutor = Executor(Runnable::run)
        return DownloadManager(
            this,
            databaseProvider,
            playerCache,
            createCacheDataSource(true),
            downloadExecutor
        )
    }

    override fun getScheduler(): Scheduler = WorkManagerScheduler(this, "MediaDownloadService")

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        throw UnsupportedOperationException()
    }
}