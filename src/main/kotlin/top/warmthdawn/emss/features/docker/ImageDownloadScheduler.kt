package top.warmthdawn.emss.features.docker

import io.ebean.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
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

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    suspend fun startDownload(id: Long, image: Image) {
        suspendCancellableCoroutine<Boolean> {
            try {
                val pull = DockerManager.pullImage(image.repository, image.tag) { status, down, speed ->
                    val result = when (status) {
                        ImageStatus.Failed, ImageStatus.Downloaded -> {
                            it.resume(status == ImageStatus.Downloaded)
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
                it.invokeOnCancellation {
                    pull.close()
                }
                pull.awaitCompletion()

                image.imageId = "${image.repository}:${image.tag}"
                image.update()
            } finally {
                downloadingMap.remove(id)
            }
        }
    }

    fun getStatus(id: Long) = downloadingMap[id]

}