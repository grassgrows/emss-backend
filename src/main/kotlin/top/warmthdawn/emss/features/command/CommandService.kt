package top.warmthdawn.emss.features.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.warmthdawn.emss.features.docker.DockerManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.coroutines.CoroutineContext

/**
 *
 * @author WarmthDawn
 * @since 2021-07-15
 */
typealias ReceiveMessage = suspend (ByteArray)->Unit
class CommandService {
    fun getAttachProxy(containerId: Long) : AttachProxy{
        TODO("获取")
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

    fun attach(containerName: String) {
        launch {
            DockerManager.attachContainer(containerName, input) {
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
