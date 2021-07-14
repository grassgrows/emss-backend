package top.warmthdawn.emss.features.file

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO
import top.warmthdawn.emss.utils.R
import java.io.File
import io.ktor.locations.KtorExperimentalLocationsAPI
import kotlin.io.path.exists
import io.ktor.locations.post as postL
import io.ktor.locations.get as getL


/**
 *
 * @author sunday7994
 * @date 2021/7/12
 */


@OptIn(KtorExperimentalLocationsAPI::class)
fun Route.fileEndpoint() {
    val fileService by inject<FileService>()

    postL<FileChunkInfoDTO> { info->
        val stream = call.receiveStream()
        stream.use {
            fileService.uploadFile(stream, info)
        }
        R.ok()
    }
    getL<FileChunkInfoDTO> { info->
//                call.respond(fileService.getFileInfo(info))
        val finalPath = "${info.destinationPath}/${info.flowRelativePath}-${info.flowChunkNumber}"
        val filePath = fileService.processPath(finalPath)
        if(filePath.exists()) {
            R.ok()
        }else{
            call.response.status(HttpStatusCode.NoContent)
        }
    }
    route("/file") {
        route("/list") {
            get() {
                val filePath = call.request.queryParameters["path"]!!
                R.ok(fileService.getFileList(filePath))
            }
        }
        route("/create") {
            post("/{dirsPath}") {
                val dirsPath = call.parameters["dirsPath"]!!
                fileService.createDirs(dirsPath)
            }
        }
        route("/download") {
            get("/{filePath}") {
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
        route("/rename") {
            post("/{filePath}") {
                val filePath = call.parameters["filePath"]!!
                val newFileName = call.receiveText()
                fileService.renameFile(filePath, newFileName)
                R.ok()
            }
        }
        route("/delete") {
            post {
                val filePaths = call.receive<Array<String>>()
                filePaths.forEach {
                    fileService.deleteFile(it)
                }
                R.ok()
            }
        }
        route("/cut") {
            post("/{target}") {
                val target = call.parameters["target"]!!
                val filePaths = call.receive<Array<String>>()
                filePaths.forEach {
                    fileService.cutFile(it, target)
                }
                R.ok()
            }
        }
        route("/copy") {
            post("/{target}") {
                val target = call.parameters["target"]!!
                val filePaths = call.receive<Array<String>>()
                filePaths.forEach {
                    fileService.copyFile(it, target)
                }
                R.ok()
            }
        }
        route("/search") {
            post("/{filePath}") {
                val filePath = call.parameters["filePath"]!!
                val keyword = call.receiveText()
                fileService.searchFile(filePath, keyword)
                R.ok()
            }
        }


    }
}
