package top.warmthdawn.emss.features.command

import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import org.koin.ktor.ext.inject

/**
 *
 * @author WarmthDawn
 * @since 2021-07-15
 */
fun Route.commandEndpoint() {

    val commandService by inject<CommandService>()
    route("/command") {
        webSocket("/attach/{id}") { // websocketSession
            val id = call.parameters["id"]!!.toLong()
            commandService.createAttach(id) {
                setCallback {
                    outgoing.send(Frame.Text(true, it))
                }
                attach()
                incoming.consumeEach {
                    receiveMessage(it.readBytes())
                }
            }
            close(CloseReason(CloseReason.Codes.NORMAL, "断开连接"))
        }
    }

}