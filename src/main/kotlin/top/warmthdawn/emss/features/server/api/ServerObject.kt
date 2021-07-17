package top.warmthdawn.emss.features.server.api

import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import top.warmthdawn.emss.features.server.entity.ServerState

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
interface ServerObject: CoroutineScope, ServerPersist {
    val id: Long
    val currentState: ServerState
    val running: Boolean
    suspend fun changeState(state: ServerState)


}