package top.warmthdawn.emss.features.server.api

import kotlinx.coroutines.CoroutineScope
import top.warmthdawn.emss.features.server.entity.ServerState

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
interface ServerObject : CoroutineScope, ServerPersist {
    val id: Long
    val currentState: ServerState
    val running: Boolean
    suspend fun changeState(state: ServerState, force: Boolean = false)


    companion object {
        fun isRunning(state: ServerState): Boolean {
            return when (state) {
                ServerState.RUNNING, ServerState.STOPPING -> true
                else -> false
            }
        }
    }
}