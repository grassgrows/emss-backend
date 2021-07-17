package top.warmthdawn.emss.features.server.api

import top.warmthdawn.emss.database.entity.ServerRealTime
import top.warmthdawn.emss.features.server.entity.ServerState
import java.time.LocalDateTime

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
interface ServerPersist {
    fun getRunning(): ServerRealTime
    fun updateRunning(lastCrashDate: LocalDateTime? = null, lastStartDate: LocalDateTime? = null)

    fun saveState(state: ServerState)
    fun getState(): ServerState
}