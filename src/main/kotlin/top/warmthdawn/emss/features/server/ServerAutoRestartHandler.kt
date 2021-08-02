package top.warmthdawn.emss.features.server

import io.ebean.Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QServerRealTime
import top.warmthdawn.emss.features.docker.DockerService
import java.time.LocalDateTime

/**
 *
 * @author WarmthDawn
 * @since 2021-07-27
 */
class ServerAutoRestartHandler(
    val db: Database,
    val dockerService: DockerService,
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    suspend fun init() {
        QServer().findIds<Long>().forEach {
            if (dockerService.isRunning(it)) {
                QServerRealTime(db)
                    .serverId.eq(it)
                    .findOne()
                    ?.apply {
                        autoRestart = false
                    }?.update()
                monitoring(it)
            }
        }
    }

    fun monitoring(serverId: Long, restart: suspend () -> Unit = {}) {
        scope.launch {
            val code = dockerService.waitContainer(serverId)
            if (code != 0L) {
                val running = QServerRealTime(db).serverId.eq(serverId).findOne()!!
                running.lastCrashDate = LocalDateTime.now()
                running.update()
                if (running.autoRestart) {
                    restart()
                }
            }
        }
    }

}