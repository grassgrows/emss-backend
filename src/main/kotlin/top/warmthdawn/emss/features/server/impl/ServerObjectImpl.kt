package top.warmthdawn.emss.features.server.impl

import io.ebean.Database
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import top.warmthdawn.emss.features.command.CommandService
import top.warmthdawn.emss.features.docker.ContainerException
import top.warmthdawn.emss.features.docker.ContainerExceptionMsg
import top.warmthdawn.emss.features.docker.ContainerStatus
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
    private val db: Database,
    override val id: Long,
    private val dockerService: DockerService,
    private val commandService: CommandService,
    private val statisticsService: StatisticsService,
    parentContext: CoroutineContext,
) : AbstractServer(),
    ServerPersist by ServerPersistImpl(db, id) {
    companion object {
        val logger = LoggerFactory.getLogger(ServerObjectImpl::class.java)
    }

    private val job = Job()
    override suspend fun stateError() {
        val state = currentState
        logger.error("状态切换异常, curr:$state")
        if (state == ServerState.STARTING || state == ServerState.STOPPING) {
            val inspect = dockerService.inspectContainer(id)
            if (inspect == ContainerStatus.Running) {
                changeState(ServerState.RUNNING, true)
            } else if (inspect == ContainerStatus.Stopped) {
                changeState(ServerState.STOPPED, true)
            }
        }
    }

    override suspend fun startComplete() {
        launch {
            dockerService.waitContainer(id)
            changeState(ServerState.STOPPED, force = true)
        }

        //连接服务器终端
        commandService.createAttach(this.id)
        commandService.sendMessage(id, "------------------------")
        commandService.sendMessage(id, "---------服务器开启-------")
        commandService.sendMessage(id, "------------------------")
        //开始服务器状态监控
        statisticsService.startMonitoring(this.id)
        updateRunning(lastStartDate = LocalDateTime.now())
    }

    override suspend fun startFailed() {
        updateRunning(lastCrashDate = LocalDateTime.now())
    }

    override suspend fun serverCrashed() {
        updateRunning(lastCrashDate = LocalDateTime.now())
        commandService.sendMessage(id, "------------------------")
        commandService.sendMessage(id, "---------服务器崩溃-------")
        commandService.sendMessage(id, "------------------------")
    }

    override suspend fun serverStop() {
        //停止服务器状态监控
        statisticsService.stopMonitoring(this.id)
        //停止连接服务器终端
        commandService.sendMessage(id, "------------------------")
        commandService.sendMessage(id, "---------服务器关闭-------")
        commandService.sendMessage(id, "------------------------")
        commandService.detach(this.id)
    }

    private suspend fun containerRunCatching(setState: Boolean = true, action: suspend () -> Unit) {
        try {
            action()
        } catch (e: ContainerException) {
            if (e.containerExceptionMsg == ContainerExceptionMsg.CONTAINER_GET_INFO_FAILED
                && setState
            ) {
                changeState(ServerState.INITIALIZE, force = true)
            }
        }
    }

    override suspend fun startContainer() = containerRunCatching {

        commandService.sendMessage(id, "------------------------")
        commandService.sendMessage(id, "-------尝试开启服务器------")
        commandService.sendMessage(id, "------------------------")
        dockerService.startContainer(this.id)
        changeState(ServerState.RUNNING, force = true)
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