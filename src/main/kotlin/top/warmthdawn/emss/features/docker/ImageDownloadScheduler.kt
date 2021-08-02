package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.command.PullImageResultCallback
import io.ebean.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import top.warmthdawn.emss.database.entity.Image
import top.warmthdawn.emss.features.docker.vo.ImageStatus
import top.warmthdawn.emss.features.docker.vo.ImageStatusVO
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

/**
 *
 * @author WarmthDawn
 * @since 2021-07-09
 */
class ImageDownloadScheduler(
    private val db: Database
) : CoroutineScope {
    private val downloadingMap: MutableMap<Long, ImageStatusVO> = ConcurrentHashMap()
    private val downloadingActions: MutableMap<Long, PullImageResultCallback> = ConcurrentHashMap()

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    suspend fun startDownload(id: Long, image: Image): Boolean {
        return withContext(coroutineContext) {
            suspendCancellableCoroutine { cont ->
                try {
                    val pull = DockerManager.pullImage(image.repository, image.tag) { status, down, speed ->
                        val result = when (status) {
                            ImageStatus.Failed, ImageStatus.Downloaded -> {
                                if (cont.isActive) {
                                    cont.resume(status == ImageStatus.Downloaded)
                                }
                                ImageStatusVO(status, 0.0, 0.0)
                            }
                            ImageStatus.Downloading -> {
                                val progress = down.values.asSequence().map { it.progress }.average()
                                ImageStatusVO(status, progress, speed)
                            }
                            else -> {
                                ImageStatusVO(status, 100.0, speed)
                            }
                        }
                        downloadingMap[id] = result
                    }
                    cont.invokeOnCancellation {
                        pull.close()
                    }
                    downloadingActions[id] = pull
                } finally {
                    downloadingActions.remove(id)
                    if (downloadingMap[id]?.status == ImageStatus.Downloading) {
                        downloadingMap[id] = ImageStatusVO(ImageStatus.Failed)
                    }
                }
            }
        }
    }

    fun getStatus(id: Long): ImageStatusVO? {
        val result = downloadingMap[id]
        if (result != null &&
            (result.status == ImageStatus.Downloaded || result.status == ImageStatus.Failed)
        ) {
            downloadingMap.remove(id)
        }
        return result
    }

    suspend fun cancelDownload(id: Long): Boolean {
        val act = downloadingActions[id] ?: return false
        return withContext(coroutineContext) {
            try {
                act.close()
                true
            } catch (e: Exception) {
                false
            }
        }

    }
}
