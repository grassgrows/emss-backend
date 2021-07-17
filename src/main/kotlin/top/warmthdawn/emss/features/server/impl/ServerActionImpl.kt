package top.warmthdawn.emss.features.server.impl

import top.warmthdawn.emss.features.docker.DockerService
import top.warmthdawn.emss.features.server.api.Actionable
import top.warmthdawn.emss.features.server.api.ServerObject
import top.warmthdawn.emss.features.server.entity.ServerState

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
class ServerActionImpl(
    private val server: ServerObject,
    private val dockerService: DockerService
) : Actionable {
    override suspend fun start() {
        server.changeState(ServerState.STARTING)
    }

    override suspend fun stop() {
        server.changeState(ServerState.STOPPING)
    }

    override suspend fun terminate() {
        if (server.currentState == ServerState.STOPPING) {
            dockerService.terminateContainer(server.id)
        }
    }

    override suspend fun restart() {
        stop()
        start()
    }
}