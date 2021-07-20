package top.warmthdawn.emss.features.server.impl

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.LoggerFactory
import top.warmthdawn.emss.features.server.api.ServerObject
import top.warmthdawn.emss.features.server.api.ServerObject.Companion.isRunning
import top.warmthdawn.emss.features.server.entity.ServerState
import kotlin.coroutines.resume

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

    private val stateWaiters = mutableListOf<Pair<ServerState, () -> Unit>>()
    protected fun addWaiter(state: ServerState, callback: () -> Unit) {
        stateWaiters.add(Pair(state, callback))
    }

    protected fun notifyWaiter(state: ServerState) {
        val waiters = stateWaiters.filter { it.first == state }
        stateWaiters.removeAll(waiters)
        waiters.forEach { it.second.invoke() }
    }

    override suspend fun waitForState(state: ServerState, timeout: Long, fallback: suspend () -> Unit) {
        if (currentState == state) {
            return
        }
        withTimeoutOrNull(timeout) {
            suspendCancellableCoroutine {
                addWaiter(state) {
                    it.resume(Unit)
                }
            }
        } ?: fallback()
    }


    override suspend fun changeState(state: ServerState, force: Boolean) {
        val current = currentState
        checkState()
        val changed = currentState
        if (state != changed) {
            saveState(state)
        }
        log.info("服务器状态切换: $current->$state")
        notifyWaiter(state)


        kotlin.runCatching {
            when (current) {
                ServerState.INITIALIZE -> when (state) {
                    ServerState.STARTING -> {
                        createContainer()
                        startContainer()
                    }
                    else -> throw UnsupportedStateChangeException("服务器未启动")
                }
                ServerState.STARTING -> when (state) {
                    ServerState.RUNNING -> {
                        startComplete()
                    }
                    ServerState.STOPPED -> {
                        startFailed()
                    }
                    else -> {
                        stateError()
                        throw UnsupportedStateChangeException("不支持的状态切换")
                    }

                }
                ServerState.RUNNING -> when (state) {
                    ServerState.INITIALIZE -> throw UnsupportedStateChangeException("不支持的状态切换")
                    ServerState.STARTING -> throw UnsupportedStateChangeException("服务器已经启动")
                    ServerState.RUNNING -> {
                    }
                    ServerState.STOPPING -> {
                        stopContainer()
                    }
                    ServerState.STOPPED -> {
                        serverCrashed()
                        serverStop()
                    }
                }
                ServerState.STOPPING -> when (state) {
                    ServerState.STOPPED -> {
                        serverStop()
                    }
                    else -> {
                        stateError()
                        throw UnsupportedStateChangeException("不支持的状态切换")
                    }

                }
                ServerState.STOPPED -> when (state) {
                    ServerState.INITIALIZE -> {
                        deleteContainer()
                    }
                    ServerState.STARTING -> {
                        startContainer()
                    }
                    ServerState.RUNNING -> {
                        startComplete()
                    }
                    else -> throw UnsupportedStateChangeException("服务器未启动")
                }
            }


        }.onFailure {
            log.error("更新服务器状态${current}->${state}失败， 强制: $force", it)
            if (!force && state != changed) {
                saveState(changed)
            }
        }


    }

    protected abstract suspend fun stateError()

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