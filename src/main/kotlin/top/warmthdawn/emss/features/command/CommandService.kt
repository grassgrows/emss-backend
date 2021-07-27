package top.warmthdawn.emss.features.command

import io.ebean.Database
import kotlinx.coroutines.*
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.features.docker.DockerManager
import top.warmthdawn.emss.features.server.ServerException
import top.warmthdawn.emss.features.server.ServerExceptionMsg
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.coroutines.resume

/**
 *
 * @author WarmthDawn
 * @since 2021-07-15
 */
typealias ReceiveMessage = suspend (ByteArray) -> Unit

class CommandService(
    private val db: Database
) {
    suspend fun createAttach(serverId: Long, config: suspend AttachProxy.() -> Unit) {
        val server =
            QServer().id.eq(serverId).findOne() ?: throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)
        val attachProxy = AttachProxy(server.containerId!!)
        config(attachProxy)
        attachProxy.detach()
    }
}

class AttachProxy(
    private val containerId: String,
    pipeBufferSize: Int = 1024 * 32,
) : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val RETURN = '\r'.code.toByte()
    private val LINE_BREAK = "\n".toByteArray()
    private val input = PipedInputStream(pipeBufferSize)
    private val output = PipedOutputStream(input)

    private var callback: suspend (ByteArray) -> Unit = {}
    fun setCallback(callback: suspend (ByteArray) -> Unit = {}) {
        this.callback = callback
    }

    private var close: suspend () -> Unit = {}
    fun setClose(close: suspend () -> Unit = {}) {
        this.close = close
    }

    fun receiveMessage(byteArray: ByteArray) {
        output.write(byteArray)
        if (byteArray.lastOrNull() == RETURN) {
            output.flush()
        }
    }


    fun detach() {
        cancel()
    }

    private fun sendToClient(msg: String) {
        launch {
            callback(msg.toByteArray())
            callback(LINE_BREAK)
        }
    }

    private fun sendToClient(msg: ByteArray) {
        launch {
            callback(msg)
        }
    }

    fun attach() {
        launch {
            suspendCancellableCoroutine<Unit> { cont ->
                sendToClient("-----成功连接服务器终端-----")
                DockerManager.logContainer(containerId) {
                    sendToClient(it.payload)

                }
                sendToClient("------以上为历史消息------")
                val result = DockerManager.attachContainer(containerId, input) {
                    sendToClient(it.payload)
                }
                cont.invokeOnCancellation {
                    result.close()
                }
                result.awaitCompletion()
                sendToClient("-----断开终端连接-----")
                cont.resume(Unit)
            }
            close()
        }
    }


}
