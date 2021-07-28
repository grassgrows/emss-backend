package top.warmthdawn.emss.features.file

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.compressed.CompressService
import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO
import top.warmthdawn.emss.features.permission.PermissionException
import top.warmthdawn.emss.utils.R
import top.warmthdawn.emss.utils.checkFilePermission
import top.warmthdawn.emss.utils.checkPermission
import top.warmthdawn.emss.utils.userId
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
    val compressService by inject<CompressService>()

    postL<FileChunkInfoDTO> { info ->
        val stream = call.receiveStream()
        checkFilePermission(0, info.destinationPath)
        fileService.uploadFile(stream, info)
        R.ok()
    }
    getL<FileChunkInfoDTO> { info ->
        checkFilePermission(0, info.destinationPath)
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
                try{
                    checkPermission(0)
                    R.ok(fileService.getFileListAdmin(filePath))
                }catch (e: PermissionException) {
                    R.ok(fileService.getFileListNormal(filePath, userId))
                }
            }
        }
        route("/newFolder") {
            post {
                val dirsPath = call.request.queryParameters["path"]!!
                checkFilePermission(0, dirsPath)
                fileService.createDirs(dirsPath)
                R.ok()
            }
        }
        route("/download") {
            get {
                val filePath = call.request.queryParameters["path"]!!
                checkFilePermission(0, filePath)
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
                checkFilePermission(0, filePath)
                val newName = call.request.queryParameters["newName"]!!
                fileService.renameFile(filePath, newName)
                R.ok()
            }
        }
        route("/delete") {
            post {
                val filePaths = call.receive<Array<String>>()
                checkFilePermission(0, *filePaths)
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
                checkFilePermission(0, *filePaths)
                filePaths.forEach {
                    fileService.cutFile(it, target + "/" + Path(it).fileName)
                }
                R.ok()
            }
        }
        route("/copy") {
            post("/check") {
                val target = call.request.queryParameters["path"]!!
                val filePaths = call.receive<Array<String>>()
                checkFilePermission(0, *filePaths)
                var count = 0
                filePaths.forEach {
                    count += fileService.findDuplicateFiles(it, target + "/" + Path(it).fileName)
                }
                R.ok(count)
            }
            post {
                val target = call.request.queryParameters["path"]!!
                val filePaths = call.receive<Array<String>>()
                checkFilePermission(0, *filePaths)
                filePaths.forEach {
                    fileService.copyFile(it, target + "/" + Path(it).fileName)
                }
                R.ok()
            }
        }
        route("/search") {
            get {
                val filePath = call.request.queryParameters["path"]!!
                checkFilePermission(0, filePath)
                val keyword = call.request.queryParameters["keyword"]!!
                R.ok(fileService.searchFile(filePath, keyword))
            }
        }
        get("/read") {
            val filePath = call.request.queryParameters["path"]!!
            val pageNum = call.request.queryParameters["pageNum"]!!
            checkFilePermission(0, filePath)
            call.respondText(fileService.readTextFile(filePath, pageNum.toInt()))
        }
        post("/save") {
            val filePath = call.request.queryParameters["path"]!!
            checkFilePermission(0, filePath)
            val text = call.receiveText()
            fileService.saveTextFile(filePath, text)
            R.ok()
        }

        post("/compress") {
            val filePaths = call.receive<Array<String>>()
            checkFilePermission(0, *filePaths)
            compressService.compressFile(filePaths)
            R.ok()
        }

        post("/uncompress") {
            val filePath = call.request.queryParameters["path"]!!
            checkFilePermission(0, filePath)
            compressService.uncompressFile(filePath)
            R.ok()
        }



    }
}