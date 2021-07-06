import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, host = "127.0.0.1", port = 8080) {
        routing {
            post {

            }
        }
    }.start(wait = true)


}
