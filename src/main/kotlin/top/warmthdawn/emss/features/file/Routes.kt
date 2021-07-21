package top.warmthdawn.emss.features.file

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO
import top.warmthdawn.emss.utils.R
import top.warmthdawn.emss.utils.checkPermission
import top.warmthdawn.emss.utils.checkServerPermission
import top.warmthdawn.emss.utils.username
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
        fileService.ensureHasAuthority(info.destinationPath, username)
        fileService.uploadFile(stream, info)
        R.ok()
    }
    getL<FileChunkInfoDTO> { info ->
        fileService.ensureHasAuthority(info.destinationPath, username)
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
                val permits = fileService.ensureHasAuthority(filePath, username)
                R.ok(fileService.getFileList(filePath).filter {
                    permits.any { perm -> it.filePath.startsWith(perm) }
                })
            }
        }
        route("/create") {
            post {
                val dirsPath = call.request.queryParameters["path"]!!
                fileService.ensureHasAuthority(dirsPath, username)
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
                fileService.ensureHasAuthority(filePath, username)
                val newName = call.request.queryParameters["newName"]!!
                fileService.renameFile(filePath, newName)
                R.ok()
            }
        }
        route("/delete") {
            post {
                val filePaths = call.receive<Array<String>>()
                filePaths.forEach {
                    fileService.ensureHasAuthority(it, username)
                }
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
                    fileService.ensureHasAuthority(it, username)
                }
                filePaths.forEach {
                    fileService.cutFile(it, target + "/" + Path(it).fileName)
                }
                R.ok()
            }
        }
        route("/copy") {
            get("/check") {
                val target = call.request.queryParameters["path"]!!
                val filePaths = call.receive<Array<String>>()
                filePaths.forEach {
                    fileService.ensureHasAuthority(it, username)
                }
                var count = 0
                filePaths.forEach {
                    count += fileService.findDuplicateFiles(it, target + "/" + Path(it).fileName)
                }
                R.ok(count)
            }
            post {
                val target = call.request.queryParameters["path"]!!
                val filePaths = call.receive<Array<String>>()
                filePaths.forEach {
                    fileService.ensureHasAuthority(it, username)
                }
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
                val permits = fileService.ensureHasAuthority(filePath, username)
                R.ok(fileService.searchFile(filePath, keyword).filter {
                    permits.any { perm -> it.filePath.startsWith(perm) }
                })
            }
        }
        route("/read") {
            get {
                val filePath = call.request.queryParameters["path"]!!
                val pageNum = call.request.queryParameters["pageNum"]!!
                fileService.ensureHasAuthority(filePath, username)
                call.respondText(fileService.readTextFile(filePath, pageNum.toInt()))
            }
            post("/save") {
                val filePath = call.request.queryParameters["path"]!!
                fileService.ensureHasAuthority(filePath, username)
                val text = call.receiveText()
                fileService.saveTextFile(filePath, text)
                R.ok()
            }
        }

    }
}