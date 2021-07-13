package top.warmthdawn.emss.features.file

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO
import top.warmthdawn.emss.utils.R


/**
 *
 * @author sunday7994
 * @date 2021/7/12
 */


fun Route.fileEndpoint() {
    val fileService by inject<FileService>()
    route("/file") {
        route("/upload"){
            post{
                val file = call.receiveMultipart()
//                val uri = call.receive<String>()
                //val info = call.receive<FileChunkInfoDTO>()
                fileService.uploadFile(file)
                R.ok()
            }
            get{
                val info = call.receive<FileChunkInfoDTO>()
                call.respond(fileService.getFileInfo(info))
            }
        }
        route("/list"){
            get("/list/{filePath}") {
                val filePath = call.parameters["filePath"]!!
                R.ok(fileService.getFileList(filePath))
            }
        }
        route("/create"){
            post("/create/{dirsPath}"){
                val dirsPath = call.parameters["dirsPath"]!!
                fileService.createDirs(dirsPath)
            }

        }



    }
}
