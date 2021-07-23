package top.warmthdawn.emss.features.command

import com.github.dockerjava.api.async.ResultCallback
import io.ebean.Database
import kotlinx.coroutines.*
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.features.docker.DockerManager
import top.warmthdawn.emss.features.server.ServerException
import top.warmthdawn.emss.features.server.ServerExceptionMsg
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
            val server = QServer().id.eq(serverId).findOne() ?: throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)
            val attachProxy = getAttachProxy(serverId)
            attachProxy.attach(server.containerId!!, detach)

            attaches.put(serverId, attachProxy)
        }
    }

    suspend fun sendMessage(id: Long, msg: String) {
        getAttachProxy(id).onMessage("$msg\n".toByteArray())
    }


    suspend fun detach(serverId: Long) {
        getAttachProxy(serverId).detach()
    }

    private val _lock = ReentrantLock()

    fun getAttachProxy(serverId: Long): AttachProxy {
        _lock.lock()
        try {
            if (!attaches.containsKey(serverId)) {
                attaches[serverId] = AttachProxy()
            }
            return attaches[serverId]!!
        }finally {
            _lock.unlock()
        }
    }
}

class AttachProxy(
    pipeBufferSize: Int = 1024 * 32,
    private val historySize: Int = 20,
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

    private suspend fun dispatchMessage(msg: ByteArray) {
        msgListeners.forEach {
            kotlin.runCatching {
                it(msg)
            }
        }
    }

    suspend fun onMessage(msg: ByteArray) {
        lock.lock()
        try {
            if (history.size > historySize) {
                history.poll()
            }
            history.offer(msg)
        } finally {
            lock.unlock()
        }
        dispatchMessage(msg)
    }

    fun attach(containerId: String, onComplete: () -> Unit = {}) {
        if (attached) {
            return
        }
        attaching = DockerManager.attachContainer(containerId, input, onComplete) {
            runBlocking {
                onMessage(it.payload)
            }
        }
        attached = true

    }

    suspend fun addListener(listener: ReceiveMessage): ReceiveMessage {
        lock.lock()
        try {
            msgListeners.add(listener)
            history.forEach { dispatchMessage(it) }
        } finally {
            lock.unlock()
        }
        dispatchMessage("成功连接终端，以上为历史消息\n".toByteArray())
        return listener
    }

    fun removeListener(listener: ReceiveMessage) {
        lock.lock()
        try {
            msgListeners.remove(listener)
        } finally {
            lock.unlock()
        }
    }

    suspend fun sendMessage(msg: ByteArray) {
        withContext(Dispatchers.IO) {
            runCatching {
                lock.lock()
                try {
                    output.write(msg)
                    if (msg.lastOrNull() == RETURN) {
                        output.flush()
                    }
                } finally {
                    lock.unlock()
                }
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
