package top.warmthdawn.emss.features.server.impl

import top.warmthdawn.emss.features.docker.DockerService
import top.warmthdawn.emss.features.server.ServerException
import top.warmthdawn.emss.features.server.ServerExceptionMsg
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
        val current = server.currentState
        if (current != ServerState.STOPPED && current != ServerState.INITIALIZE) {
            throw ServerException(ServerExceptionMsg.SERVER_NOT_STOPPED)
        }
        server.changeState(ServerState.STARTING)
    }

    override suspend fun stop() {
        val current = server.currentState
        if (current != ServerState.RUNNING) {
            throw ServerException(ServerExceptionMsg.SERVER_NOT_RUNNING)
        }
        server.changeState(ServerState.STOPPING)

        server.waitForState(ServerState.STOPPED, 30 * 1000L) {
            server.saveState(ServerState.RUNNING)
        }
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