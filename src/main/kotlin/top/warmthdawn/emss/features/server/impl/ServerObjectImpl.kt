package top.warmthdawn.emss.features.server.impl

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import top.warmthdawn.emss.features.command.CommandService
import top.warmthdawn.emss.features.docker.ContainerException
import top.warmthdawn.emss.features.docker.ContainerExceptionMsg
import top.warmthdawn.emss.features.docker.DockerService
import top.warmthdawn.emss.features.server.api.ServerPersist
import top.warmthdawn.emss.features.server.entity.ServerState
import java.time.LocalDateTime
import kotlin.coroutines.CoroutineContext

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
class ServerObjectImpl(
    val id: Long,
    val dockerService: DockerService,
    val commandService: CommandService,
    parentContext: CoroutineContext,
) : AbstractServer(),
    ServerPersist by ServerPersistImpl(id) {
    private val job = Job()


    override suspend fun startComplete() {
        commandService.createAttach(this.id) {
            launch {
                changeState(ServerState.STOPPED)
            }
        }
        updateRunning(lastStartDate = LocalDateTime.now())
    }

    override suspend fun startFailed() {
        updateRunning(lastCrashDate = LocalDateTime.now())
    }

    override suspend fun serverCrashed() {
        updateRunning(lastCrashDate = LocalDateTime.now())
    }

    override suspend fun serverStop() {

    }

    private suspend fun containerRunCatching(setState: Boolean = true, action: suspend () -> Unit) {
        try {
            action()
        } catch (e: ContainerException) {
            if (e.containerExceptionMsg == ContainerExceptionMsg.CONTAINER_GET_INFO_FAILED
                && setState
            ) {
                changeState(ServerState.INITIALIZE)
            }
        }
    }

    override suspend fun startContainer() = containerRunCatching {
        dockerService.startContainer(this.id)
        changeState(ServerState.RUNNING)
    }

    override suspend fun stopContainer() = containerRunCatching {
        dockerService.stopContainer(this.id)
    }

    override suspend fun deleteContainer() = containerRunCatching(false) {
        dockerService.removeContainer(this.id)
    }

    override suspend fun createContainer() = containerRunCatching {
        dockerService.createContainer(this.id)
    }


    override val coroutineContext: CoroutineContext =
        CoroutineName("ServerObject($id)")
            .plus(parentContext)
            .plus(job)

}