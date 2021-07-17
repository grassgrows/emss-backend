package top.warmthdawn.emss.features.command

import com.github.dockerjava.api.async.ResultCallback
import kotlinx.coroutines.*
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QServerRealTime
import top.warmthdawn.emss.features.docker.DockerManager
import top.warmthdawn.emss.features.server.ServerException
import top.warmthdawn.emss.features.server.ServerExceptionMsg
import top.warmthdawn.emss.features.server.ServerStatus
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.CoroutineContext

/**
 *
 * @author WarmthDawn
 * @since 2021-07-15
 */
typealias ReceiveMessage = suspend (ByteArray) -> Unit

class CommandService {
    private val attaches: MutableMap<Long, AttachProxy> = mutableMapOf()

    suspend fun createAttach(serverId: Long, detach: ()->Unit) {
        coroutineScope {
            val server = QServer().id.eq(serverId).findOne()
            val serverRealTime = QServerRealTime().serverId.eq(serverId).findOne()
            if (server == null || serverRealTime == null)
                throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)
            val attachProxy = attaches.getOrDefault(serverId, AttachProxy())
            if (serverRealTime.status == ServerStatus.Running) {
                attachProxy.attach(server.containerId!!, detach)
                attaches.put(serverId, attachProxy)
            } else {
                throw ServerException(ServerExceptionMsg.SERVER_NOT_RUNNING)
            }
        }
    }

    fun getAttachProxy(serverId: Long): AttachProxy {
        if(!attaches.containsKey(serverId)){
            attaches[serverId] = AttachProxy()
        }
        return attaches.get(serverId)!!
    }
}

class AttachProxy(
    pipeBufferSize: Int = 1024 * 32,
    private val historySize: Int = 10,
    context: CoroutineContext? = null
) : CoroutineScope {
    private val msgListeners: MutableSet<ReceiveMessage> = mutableSetOf()
    private val input = PipedInputStream(pipeBufferSize)
    private val output = PipedOutputStream(input)
    private val lock = ReentrantLock()
    private val EOL = "\n".toByteArray()
    private val RETURN = "\r".toByteArray()[0]
    override val coroutineContext: CoroutineContext = if (context == null) Dispatchers.IO else context + Dispatchers.IO
    private lateinit var attaching: ResultCallback<*>
    private var attached = false


    private val history = LinkedList<ByteArray>()

    private suspend fun onMessage(msg: ByteArray) {
        msgListeners.forEach {
            it(msg)
        }
    }

    fun attach(containerId: String, onComplete: ()->Unit = {}) {
        if (attached) {
            return
        }
        attaching = DockerManager.attachContainer(containerId, input, onComplete) {
            if (history.size > historySize) {
                history.poll()
            }
            runBlocking {
                history.offer(it.payload)
                onMessage(it.payload)
            }
        }
    }

    suspend fun addListener(listener: ReceiveMessage): ReceiveMessage {
        lock.lock()
        history.forEach { onMessage(it) }
        msgListeners.add(listener)
        lock.unlock()
        return listener
    }

    fun removeListener(listener: ReceiveMessage) {
        lock.lock()
        msgListeners.remove(listener)
        lock.unlock()
    }

    suspend fun sendMessage(msg: ByteArray) {
        lock.lock()
        output.write(msg)
        if (msg.lastOrNull() == RETURN) {
            onMessage(msg + EOL)
        } else {
            onMessage(msg)
        }
        lock.unlock()
    }

    fun detach() {
        if (!attached) {
            if (::attaching.isInitialized) {
                attaching.close()
            }
            coroutineContext.cancel()
        }
    }
}
