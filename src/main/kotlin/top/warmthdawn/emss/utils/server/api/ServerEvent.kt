package top.warmthdawn.emss.utils.server.api

import top.warmthdawn.emss.features.server.entity.ServerState
import top.warmthdawn.emss.utils.event.api.Event

/**
 *
 * @author WarmthDawn
 * @since 2021-07-20
 */

object ServerStateChange : Event<Pair<ServerState, ServerState>>

enum class ServerContainerEvent: Event<ServerInstance> {
    START,
    STOP,
    CREATED,
    REMOVED,
}

enum class ServerRunningEvent: Event<ServerInstance> {
    START,
    CRASH,
    STOP
}