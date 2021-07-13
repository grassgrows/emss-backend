package top.warmthdawn.emss.misc

import io.ktor.util.*
import java.net.URI
import kotlin.io.path.Path


fun main() {

    val input = "/root/sky/et2/../world/saves/players"
    var uri = URI(input).normalize().path

    if (uri == "/") {
        //
    }

    uri = if (uri.startsWith("/")) uri.substring(1) else uri

    val type = uri.substringBefore('/')
    when (type) {
        "root" -> {
            val relativePath = type.substringAfter("root/")
            //用户权限
            val serverLocations = arrayOf("sky/et2", "timw4")
            if (serverLocations.any { relativePath.startsWith(it) }) {

                val root = Path("D:\\emss\\root")
                println(root.combineSafe(Path(relativePath)))

            } else {
                //404
            }
        }
        "backup" -> {

        }
        else -> {

        }
    }
}
