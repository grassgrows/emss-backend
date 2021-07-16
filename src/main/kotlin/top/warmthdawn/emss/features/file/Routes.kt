package top.warmthdawn.emss.features.file

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO
import top.warmthdawn.emss.utils.R
import kotlin.io.path.Path
import io.ktor.locations.get as getL
import io.ktor.locations.post as postL


/**
 *
 * @author sunday7994
 * @date 2021/7/12
 */


@OptIn(KtorExperimentalLocationsAPI::class)
fun Route.fileEndpoint() {
    val fileService by inject<FileService>()

    postL<FileChunkInfoDTO> { info ->
        val stream = call.receiveStream()
        fileService.uploadFile(stream, info)
        R.ok()
    }
    getL<FileChunkInfoDTO> { info ->
        if (fileService.testFile(info)) {
            R.ok()
        } else {
            call.response.status(HttpStatusCode.NoContent)
        }
    }
    route("/file") {
        route("/list") {
            get {
                val filePath = call.request.queryParameters["path"]!!
                R.ok(fileService.getFileList(filePath))
            }
        }
        route("/create") {
            post {
                val dirsPath = call.request.queryParameters["path"]!!
                fileService.createDirs(dirsPath)
                R.ok()
            }
        }
        route("/download") {
            get {
                val filePath = call.request.queryParameters["path"]!!
                val file = fileService.processPath(filePath).toFile()
                if (!file.exists()) {
                    throw FileException(FileExceptionMsg.FILE_NOT_FOUND)
                }
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, file.name)
                        .toString()
                )
                call.respondFile(file)
            }
        }
        route("/rename") {
            post {
                val filePath = call.request.queryParameters["path"]!!
                val newName = call.request.queryParameters["newName"]!!
                fileService.renameFile(filePath, newName)
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
            post {
                val target = call.request.queryParameters["path"]!!
                val filePaths = call.receive<Array<String>>()
                filePaths.forEach {
                    fileService.cutFile(it, target + "/" + Path(it).fileName)
                }
                R.ok()
            }
        }
        route("/copy") {
            post {
                val target = call.request.queryParameters["path"]!!
                val filePaths = call.receive<Array<String>>()
                filePaths.forEach {
                    fileService.copyFile(it, target + "/" + Path(it).fileName)
                }
                R.ok()
            }
        }
        route("/search") {
            get {
                val filePath = call.request.queryParameters["path"]!!
                val keyword = call.request.queryParameters["keyword"]!!
                R.ok(fileService.searchFile(filePath, keyword))
            }
        }


    }
}