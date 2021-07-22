package top.warmthdawn.emss.utils.server.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import top.warmthdawn.emss.database.entity.ServerRealTime
import top.warmthdawn.emss.features.server.entity.ServerState
import top.warmthdawn.emss.utils.event.api.EventEmitter
import java.time.LocalDateTime

/**
 *
 * @author WarmthDawn
 * @since 2021-07-20
 */
interface ServerInstance : EventEmitter {
    val id: Long

    //服务器控制相关
    suspend fun start()
    suspend fun stop()
    suspend fun terminate()
    suspend fun restart()
    suspend fun reset()

    //服务器状态相关
    suspend fun isRunning(): Boolean
    fun getRunningInfo(): ServerRealTime
    fun updateRunning(lastCrashDate: LocalDateTime? = null, lastStartDate: LocalDateTime? = null)
    suspend fun syncState()
    suspend fun getState(): ServerState
    val currentAction: ServerAction

    //服务器运行相关
    suspend fun beginMonitoring()
    suspend fun stopMonitoring()
    val scope: CoroutineScope?
}