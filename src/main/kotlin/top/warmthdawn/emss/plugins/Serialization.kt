package top.warmthdawn.emss.plugins

import io.ktor.serialization.*
import io.ktor.features.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import java.text.DateFormat

fun Application.configureSerialization() {
    install(ContentNegotiation) {
//        json()
        jackson {

        }
    }
}
