package top.warmthdawn.emss.plugins

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import java.time.Duration

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(5)
        timeout = Duration.ofSeconds(5)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}
