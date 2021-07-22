package top.warmthdawn.emss.features.docker

import io.ebean.DB
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

fun Route.dockerEndpoint() {


    route("/docker") {
        get {

        }
    }

}