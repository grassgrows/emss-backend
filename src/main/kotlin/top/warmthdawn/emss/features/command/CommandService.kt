package top.warmthdawn.emss.features.command

import com.github.dockerjava.api.async.ResultCallback
import io.ebean.Database
import kotlinx.coroutines.*
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QServerRealTime
import top.warmthdawn.emss.features.docker.DockerManager
import top.warmthdawn.emss.features.server.ServerException
import top.warmthdawn.emss.features.server.ServerExceptionMsg
import top.warmthdawn.emss.features.server.entity.ServerState
import top.warmthdawn.emss.features.server.impl.ServerPersistImpl
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

class CommandService(
    private val db: Database
) {
    private val attaches: MutableMap<Long, AttachProxy> = mutableMapOf()

    suspend fun createAttach(serverId: Long, detach: () -> Unit = {}) {
        coroutineScope {
            val server = QServer().id.eq(serverId).findOne()
            val serverRealTime = QServerRealTime().serverId.eq(serverId).findOne()
            if (server == null || serverRealTime == null)
                throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)
            val attachProxy = getAttachProxy(serverId)
            if(serverRealTime.serverState == ServerState.RUNNING){
                attachProxy.attach(server.containerId!!, detach)
            }
            attaches.put(serverId, attachProxy)
        }
    }

    suspend fun sendMessage(id: Long, msg: String) {
        getAttachProxy(id).onMessage("$msg\n".toByteArray())
    }


    suspend fun detach(serverId: Long) {
        getAttachProxy(serverId).detach()
    }

    suspend fun getAttachProxy(serverId: Long): AttachProxy {

        if (!attaches.containsKey(serverId)) {
            attaches[serverId] = AttachProxy()
            if (ServerPersistImpl(db, serverId).getState() == ServerState.RUNNING) {
                createAttach(serverId)
            }
        }
        return attaches[serverId]!!
    }
}

class AttachProxy(
    pipeBufferSize: Int = 1024 * 32,
    private val historySize: Int = 10,
    context: CoroutineContext? = null
) : CoroutineScope {
    private val RETURN = '\r'.code.toByte()
    private val msgListeners: MutableSet<ReceiveMessage> = mutableSetOf()
    private val input = PipedInputStream(pipeBufferSize)
    private val output = PipedOutputStream(input)
    private val lock = ReentrantLock()
    override val coroutineContext: CoroutineContext = if (context != null) {
        CoroutineName("AttachProxy").plus(Dispatchers.IO).plus(context)
    } else CoroutineName("AttachProxy").plus(Dispatchers.IO)

    private lateinit var attaching: ResultCallback<*>
    private var attached = false


    private val history = LinkedList<ByteArray>()

    suspend fun onMessage(msg: ByteArray) {
        msgListeners.forEach {
            kotlin.runCatching {
                it(msg)
            }
        }
    }

    fun attach(containerId: String, onComplete: () -> Unit = {}) {
        if (attached) {
            return
        }
        attaching = DockerManager.attachContainer(containerId, input, onComplete) {
            lock.lock()
            if (history.size > historySize) {
                history.poll()
            }
            history.offer(it.payload)
            lock.unlock()
            runBlocking {
                onMessage(it.payload)
            }
        }
        attached = true

    }

    suspend fun addListener(listener: ReceiveMessage): ReceiveMessage {
        lock.lock()
        msgListeners.add(listener)
        history.forEach { onMessage(it) }
        lock.unlock()
        return listener
    }

    fun removeListener(listener: ReceiveMessage) {
        lock.lock()
        msgListeners.remove(listener)
        lock.unlock()
    }

    suspend fun sendMessage(msg: ByteArray) {
        withContext(Dispatchers.IO) {
            runCatching {
                lock.lock()
                output.write(msg)
                if (msg.lastOrNull() == RETURN) {
                    output.flush()
                }
                lock.unlock()
            }
        }
    }

    fun detach() {
        if (!attached) {
            if (::attaching.isInitialized) {
                attaching.close()
            }
            coroutineContext.cancel()
        }

        attached = false
    }
}
