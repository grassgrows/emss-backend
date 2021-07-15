package top.warmthdawn.emss.features.command

import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.docker.ContainerService

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
            val proxy = commandService.getAttachProxy(id)
            val listener = proxy.addListener {
                outgoing.send(Frame.Text(true, it))
            }
            while (true) {
                try {
                    val frame = incoming.receive()
                    proxy.sendMessage(frame.readBytes())
                }catch (e: Exception){
                    break
                }
            }
            proxy.removeListener(listener)
            close(CloseReason(CloseReason.Codes.NORMAL, "ConnectClosed"))
        }
    }

}