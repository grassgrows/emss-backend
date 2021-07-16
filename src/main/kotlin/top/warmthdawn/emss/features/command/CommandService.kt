package top.warmthdawn.emss.features.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QServerRealTime
import top.warmthdawn.emss.features.docker.DockerManager
import top.warmthdawn.emss.features.server.ServerException
import top.warmthdawn.emss.features.server.ServerExceptionMsg
import top.warmthdawn.emss.features.server.ServerStatus
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.CoroutineContext

/**
 *
 * @author WarmthDawn
 * @since 2021-07-15
 */
typealias ReceiveMessage = suspend (ByteArray)->Unit
class CommandService {
    fun getAttachProxy(containerId: Long): AttachProxy {
        val server = QServer().id.eq(containerId).findOne()
        val serverRealTime = QServerRealTime().id.eq(containerId).findOne()
        if (server == null || serverRealTime == null)
            throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)

        val attachProxy = AttachProxy()
        if (serverRealTime.status == ServerStatus.Running)
        {
            attachProxy.attach(server.containerId!!)
            return attachProxy
        }
        else
        {
            throw ServerException(ServerExceptionMsg.SERVER_NOT_RUNNING)
        }
    }
}


class AttachProxy(
    bufferSize: Int = 1024*32,
    context: CoroutineContext? = null
): CoroutineScope {
    private val msgListeners: MutableSet<ReceiveMessage> = mutableSetOf()
    private val input = PipedInputStream(bufferSize)
    private val output = PipedOutputStream(input)
    private val lock = ReentrantLock()

    override val coroutineContext: CoroutineContext = if(context == null) Dispatchers.IO else context + Dispatchers.IO

    private fun onMessage(msg: ByteArray) {
        msgListeners.forEach {
            launch {
                it(msg)
            }
        }
    }

    fun attach(containerId: String) {
        launch {
            DockerManager.attachContainer(containerId, input) {
                onMessage(it.payload)
            }
        }
    }

    fun addListener(listener: ReceiveMessage): ReceiveMessage {
        lock.lock()
        msgListeners.add(listener)
        lock.unlock()
        return listener
    }

    fun removeListener(listener: ReceiveMessage) {
        lock.lock()
        msgListeners.remove(listener)
        lock.unlock()
    }

    fun sendMessage(msg: ByteArray) {
        lock.lock()
        output.write(msg)
        lock.unlock()
    }
}
