package top.warmthdawn.emss.features.server.impl

import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import top.warmthdawn.emss.features.server.api.ServerObject
import top.warmthdawn.emss.features.server.api.ServerObject.Companion.isRunning
import top.warmthdawn.emss.features.server.entity.ServerState

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
abstract class AbstractServer : ServerObject {
    companion object {
        private val log = LoggerFactory.getLogger(AbstractServer::class.java)
    }

    override val currentState: ServerState
        get() = getState()

    override val running
        get() = isRunning(currentState)

    override suspend fun changeState(state: ServerState, force: Boolean) {
        val current = currentState
        kotlin.runCatching {
            val run = when (current) {
                ServerState.INITIALIZE -> when (state) {
                    ServerState.STARTING -> launch {
                        createContainer()
                        startContainer()
                    }
                    else -> throw UnsupportedStateChangeException("服务器未启动")
                }
                ServerState.STARTING -> when (state) {
                    ServerState.RUNNING -> launch {
                        startComplete()
                    }
                    ServerState.STOPPED -> launch {
                        startFailed()
                    }
                    else -> throw UnsupportedStateChangeException("不支持的状态切换")

                }
                ServerState.RUNNING -> when (state) {
                    ServerState.INITIALIZE -> throw UnsupportedStateChangeException("不支持的状态切换")
                    ServerState.STARTING -> throw UnsupportedStateChangeException("服务器已经启动")
                    ServerState.RUNNING -> launch {
                    }
                    ServerState.STOPPING -> launch {
                        stopContainer()
                    }
                    ServerState.STOPPED -> launch {
                        serverCrashed()
                        serverStop()
                    }
                }
                ServerState.STOPPING -> when (state) {
                    ServerState.STOPPED -> launch {
                        serverStop()
                    }
                    else -> throw UnsupportedStateChangeException("不支持的状态切换")

                }
                ServerState.STOPPED -> when (state) {
                    ServerState.INITIALIZE -> launch {
                        deleteContainer()
                    }
                    ServerState.STARTING -> launch {
                        startContainer()
                    }
                    ServerState.RUNNING -> launch {
                        startComplete()
                    }
                    else -> throw UnsupportedStateChangeException("服务器未启动")
                }
            }

            saveState(state)
            run.join()
        }.onFailure {
            log.error("更新服务器状态${current}->${state}失败， 强制: $force", it)
            if (force)
                saveState(state)
            throw it

        }


    }

    protected abstract suspend fun startComplete()
    protected abstract suspend fun startFailed()
    protected abstract suspend fun serverCrashed()
    protected abstract suspend fun serverStop()


    protected abstract suspend fun startContainer()
    protected abstract suspend fun stopContainer()
    protected abstract suspend fun deleteContainer()
    protected abstract suspend fun createContainer()
}

class UnsupportedStateChangeException(msg: String) : UnsupportedOperationException(msg)