package top.warmthdawn.emss.features.file

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.ContentDisposition.Companion.File
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO
import top.warmthdawn.emss.utils.R
import java.io.File


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
            get("/{filePath}") {
                val filePath = call.parameters["filePath"]!!
                R.ok(fileService.getFileList(filePath))
            }
        }
        route("/create"){
            post("/{dirsPath}"){
                val dirsPath = call.parameters["dirsPath"]!!
                fileService.createDirs(dirsPath)
            }
        }
        route("/download"){
            get("/{filePath}"){
                val filePath = call.parameters["filePath"]!!
                val file = File(fileService.processPath(filePath).toString())
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, file.name)
                        .toString()
                )
                call.respondFile(file)
            }
        }
        route("/rename"){
            post("/{filePath}"){
                val filePath = call.parameters["filePath"]!!
                val newFileName = call.receiveText()
                fileService.renameFile(filePath, newFileName)
                R.ok()
            }
        }
        route("/delete"){
            post(){
                val filePaths = call.receive<Array<String>>()
                filePaths.forEach {
                    fileService.deleteFile(it)
                }
                R.ok()
            }
        }
        route("/cut"){
            post("/{target}"){
                val target = call.parameters["target"]!!
                val filePaths = call.receive<Array<String>>()
                filePaths.forEach {
                    fileService.cutFile(it, target)
                }
                R.ok()
            }
        }
        route("/copy"){
            post("/{target}"){
                val target = call.parameters["target"]!!
                val filePaths = call.receive<Array<String>>()
                filePaths.forEach {
                    fileService.copyFile(it, target)
                }
                R.ok()
            }
        }
        route("/search"){}




    }
}
