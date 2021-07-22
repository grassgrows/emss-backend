package top.warmthdawn.emss.utils.server.impl

import io.ebean.Database
import kotlinx.coroutines.*
import top.warmthdawn.emss.database.entity.ServerRealTime
import top.warmthdawn.emss.database.entity.query.QServerRealTime
import top.warmthdawn.emss.features.command.CommandService
import top.warmthdawn.emss.features.docker.ContainerStatus
import top.warmthdawn.emss.features.docker.DockerService
import top.warmthdawn.emss.features.server.ServerException
import top.warmthdawn.emss.features.server.ServerExceptionMsg
import top.warmthdawn.emss.features.server.entity.ServerState
import top.warmthdawn.emss.features.statistics.impl.StatisticsService
import top.warmthdawn.emss.utils.event.api.EventEmitter
import top.warmthdawn.emss.utils.event.impl.DefaultEventEmitter
import top.warmthdawn.emss.utils.server.api.*
import java.time.LocalDateTime
import kotlin.coroutines.resume

/**
 *
 * @author WarmthDawn
 * @since 2021-07-20
 */
class ServerInstanceImpl(
    override val id: Long,
    private val db: Database,
    private val dockerService: DockerService,
    private val commandService: CommandService,
    private val statisticsService: StatisticsService
) : ServerInstance,
    EventEmitter by DefaultEventEmitter() {
    override var scope: CoroutineScope? = null

    init {
        //服务器事件
        on(ServerRunningEvent.START) {
            updateRunning(lastStartDate = LocalDateTime.now())
            commandService.sendMessage(id, "------------------------")
            commandService.sendMessage(id, "---------服务器开启-------")
            commandService.sendMessage(id, "------------------------")
        }
        on(ServerRunningEvent.STOP) {
            commandService.sendMessage(id, "------------------------")
            commandService.sendMessage(id, "---------服务器关闭-------")
            commandService.sendMessage(id, "------------------------")
        }
        on(ServerRunningEvent.CRASH) {
            updateRunning(lastCrashDate = LocalDateTime.now())
            commandService.sendMessage(id, "------------------------")
            commandService.sendMessage(id, "---------服务器崩溃-------")
            commandService.sendMessage(id, "------------------------")
        }


        //容器事件
        on(ServerContainerEvent.STOP) {
            syncState()
            stopMonitoring()
            if (currentAction != ServerAction.START) {
                emit(ServerRunningEvent.CRASH, it)
            } else {
                emit(ServerRunningEvent.STOP, it)
            }
            currentAction = ServerAction.NONE
        }
        on(ServerContainerEvent.START) {
            syncState()
            beginMonitoring()
            if (currentAction == ServerAction.TERMINATE || currentAction == ServerAction.STOP) {
                emit(ServerRunningEvent.START, it)
            }
            currentAction = ServerAction.NONE
        }
        on(ServerContainerEvent.CREATED) {
            syncState()
            currentAction = ServerAction.NONE
        }
        on(ServerContainerEvent.REMOVED) {
            syncState()
            currentAction = ServerAction.NONE
        }

    }

    private var inited = false

    suspend fun init() {
        if(inited) {
            return
        }
        inited = true
        syncState()
        if(isRunning()){
            beginMonitoring()
        }
    }

    override suspend fun isRunning(): Boolean {
        val state = getState()
        return state == ServerState.RUNNING || state == ServerState.STOPPING

    }

    //开启服务器
    override suspend fun start() {
        val current = getState()
        if (current == ServerState.REMOVING) {
            throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)
        }
        if (current != ServerState.STOPPED && current != ServerState.INITIALIZE) {
            throw ServerException(ServerExceptionMsg.SERVER_NOT_STOPPED)
        }

        if (current == ServerState.INITIALIZE) {
            currentAction = ServerAction.RESET
            dockerService.createContainer(id)
            emit(ServerContainerEvent.CREATED, this)
        }
        currentAction = ServerAction.START
        dockerService.startContainer(id)
        emit(ServerContainerEvent.START, this)

    }

    override suspend fun stop() {
        val current = getState()
        if (current == ServerState.REMOVING) {
            throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)
        }
        if (current != ServerState.RUNNING) {
            throw ServerException(ServerExceptionMsg.SERVER_NOT_RUNNING)
        }
        currentAction = ServerAction.STOP
        dockerService.stopContainer(id)

    }

    override suspend fun terminate() {
        currentAction = ServerAction.TERMINATE
        kotlin.runCatching {
            dockerService.terminateContainer(id)
        }
    }

    override suspend fun restart() {
        stop()
        suspendCancellableCoroutine<ServerInstance> { cont ->
            once(ServerContainerEvent.STOP) {
                cont.resume(it)
            }
        }
        start()
    }

    private suspend fun resetServer() {
        emit(ServerContainerEvent.REMOVED, this)
        currentAction = ServerAction.RESET
    }

    override suspend fun reset() {
        val current = getState()
        if (current == ServerState.REMOVING) {
            throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)
        }
        if (current != ServerState.STOPPED) {
            throw ServerException(ServerExceptionMsg.SERVER_NOT_STOPPED)
        }
        dockerService.removeContainer(id)
        resetServer()
    }

    override fun getRunningInfo(): ServerRealTime {
        return QServerRealTime(db).id.eq(id).findOne()!!
    }

    override fun updateRunning(lastCrashDate: LocalDateTime?, lastStartDate: LocalDateTime?) {
        QServerRealTime(db).id.eq(id).findOne()?.let {
            if (lastCrashDate != null) {
                it.lastCrashDate = lastCrashDate
            }
            if (lastStartDate != null) {
                it.lastStartDate = lastStartDate
            }
            it.update()
        }
    }

    private var oldState = ServerState.INITIALIZE
    override suspend fun syncState() {
        val newState = getState()
        if (oldState != newState) {
            this.emit(ServerStateChange, Pair(oldState, newState))
            oldState = newState
        }
    }

    override suspend fun getState(): ServerState {

        val dockerState = dockerService.inspectContainer(id)
        if (dockerState == ContainerStatus.Stopped) {
            return when (currentAction) {
                ServerAction.RESET -> ServerState.INITIALIZE
                ServerAction.REMOVE -> ServerState.REMOVING
                ServerAction.STOP, ServerAction.TERMINATE, ServerAction.NONE -> ServerState.STOPPED
                ServerAction.START -> ServerState.STARTING
            }
        }

        if (dockerState == ContainerStatus.Running) {
            return when (currentAction) {
                ServerAction.RESET -> ServerState.INITIALIZE
                ServerAction.REMOVE -> ServerState.REMOVING
                ServerAction.STOP, ServerAction.TERMINATE -> ServerState.STOPPING
                ServerAction.START, ServerAction.NONE -> ServerState.RUNNING
            }
        }

        if (dockerState == ContainerStatus.Removed && currentAction != ServerAction.RESET) {
            try {
                resetServer()
            } catch (e: ServerException) {
            }
            return ServerState.INITIALIZE
        }

        return ServerState.INITIALIZE
    }

    override var currentAction: ServerAction = ServerAction.NONE
        private set

    override suspend fun beginMonitoring() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            dockerService.waitContainer(id)
        }.invokeOnCompletion {
            emit(ServerContainerEvent.STOP, this)
        }
        scope.launch {
            while (isActive) {
                delay(10 * 1000L)
                syncState()
            }
        }
        this.scope = scope

        //连接服务器终端
        commandService.createAttach(this.id)
        //开始服务器状态监控
        statisticsService.startMonitoring(this.id)
    }


    override suspend fun stopMonitoring() {
        scope?.cancel()
        scope = null
        kotlin.runCatching {
            //停止服务器状态监控
            statisticsService.stopMonitoring(this.id)
        }
        //停止连接服务器终端
        commandService.detach(this.id)
    }

}