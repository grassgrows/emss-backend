package top.warmthdawn.emss.features.file

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO
import top.warmthdawn.emss.utils.R
import java.io.File
import io.ktor.locations.KtorExperimentalLocationsAPI
import kotlin.io.path.exists
import io.ktor.locations.post as postL
import io.ktor.locations.get as getL
import kotlin.io.path.Path


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
            post() {
                val dirsPath = call.request.queryParameters["path"]!!
                fileService.createDirs(dirsPath)
                R.ok()
            }
        }
        route("/download") {
            get() {
                val filePath = call.request.queryParameters["path"]!!
                val file = fileService.processPath(filePath).toFile()
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, file.name)
                        .toString()
                )
                call.respondFile(file)
            }
        }
        route("/rename") {
            post() {
                val filePath = call.request.queryParameters["path"]!!
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
            post() {
                val target = call.request.queryParameters["path"]!!
                val filePaths = call.receive<Array<String>>()
                filePaths.forEach {
                    fileService.cutFile(it, target + "/" + Path(it).fileName)
                }
                R.ok()
            }
        }
        route("/copy") {
            post() {
                val target = call.request.queryParameters["path"]!!
                val filePaths = call.receive<Array<String>>()
                filePaths.forEach {
                    fileService.copyFile(it, target + "/" + Path(it).fileName)
                }
                R.ok()
            }
        }
        route("/search") {
            post() {
                val filePath = call.request.queryParameters["path"]!!
                val keyword = call.receiveText()
                fileService.searchFile(filePath, keyword)
                R.ok()
            }
        }


    }
}
