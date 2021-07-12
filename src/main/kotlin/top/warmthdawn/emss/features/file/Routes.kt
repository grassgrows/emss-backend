package top.warmthdawn.emss.features.file

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO
import java.io.File


/**
 *
 * @author sunday7994
 * @date 2021/7/12
 */


fun Route.fileEndpoint() {
    val fileService by inject<FileService>()
    route("/upload") {


        post {
            val file = call.receiveMultipart()
            //val info = call.receive<FileChunkInfoDTO>()
            fileService.uploadFile(file)
            call.respond(HttpStatusCode.OK)
        }
    }
}
