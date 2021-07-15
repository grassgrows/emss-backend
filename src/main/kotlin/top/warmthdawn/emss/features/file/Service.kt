package top.warmthdawn.emss.features.file

import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO
import top.warmthdawn.emss.features.file.vo.FileListInfoVO
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream
import java.net.URI
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.invariantSeparatorsPathString


/**
 *
 * @author sunday7994
 * @date 2021/7/12
 */

class FileService {

    fun processPath(input: String): Path {

        var uri = Path(input).normalize().invariantSeparatorsPathString

        if (uri == "/" || uri == "") {
            uri = "/root/"
        }

        uri = if (uri.startsWith("/")) uri.substring(1) else uri

        when (uri.substringBefore('/')) {
            "root" -> {
                val root = Path(QSetting().type.eq(SettingType.SERVER_ROOT_DIRECTORY).findOne()!!.value)
                val relativePath = uri.substringAfter("root")
                //用户权限
//               val serverLocations = arrayOf("sky/et2", "timw4")
//                if (serverLocations.any { relativePath.startsWith(it) }) {
//                    return root.combineSafe(Path(relativePath)).toPath()
//                } else {
//                    throw PathException(PathExceptionMsg.INSUFFICIENT_PERMISSION_LEVEL)
//                }
//              TODO("用户权限搞一下")
                return root.combineSafe(Path(relativePath)).toPath()
            }
//            "backup" -> {
//
//            }
            else -> {
                throw PathException(PathExceptionMsg.WRONG_PATH_FORMAT)
            }
        }
    }

    private fun processTempPath(identifier: String, chunkNumber: Int, isTemp: Boolean = false): Path {
        val root = Path(QSetting().type.eq(SettingType.TEMPORARY_FOLDER).findOne()!!.value)
        val chunkString = chunkNumber.toString().padStart(5, '0')
        val relativePath = if (isTemp) {
            "/$identifier}/chunk-${chunkString}.emssdownloading"
        } else {
            "/$identifier}/chunk-${chunkString}.emsschunk"
        }
        return root.combineSafe(Path(relativePath)).toPath()
    }

    private fun processFinalPath(destinationPath: String, flowRelativePath: String): Path {
        val filePathRaw = FileChunkManager.getFinalPath(destinationPath, flowRelativePath)
        createDirs(processPath(filePathRaw).toString())
        return processPath(filePathRaw)
    }

    suspend fun testFile(info: FileChunkInfoDTO): Boolean {
        FileChunkManager.validateRequest(info)
        val filePathChunk = processTempPath(info.flowIdentifier, info.flowChunkNumber)
        return filePathChunk.exists()
    }

    suspend fun uploadFile(input: InputStream, info: FileChunkInfoDTO) {
        FileChunkManager.validateRequest(info)
        withContext(Dispatchers.IO) {
            val filePathDownloading = processTempPath(info.flowIdentifier, info.flowChunkNumber, true)
            filePathDownloading.toFile().outputStream().use { out ->
                val transferred = input.copyTo(out)
                FileChunkManager.fileChunkCheck(info, transferred.toInt())
            }

            val filePathChunk = processTempPath(info.flowIdentifier, info.flowChunkNumber)
            //下载完成 改名
            filePathDownloading.toFile().renameTo(filePathChunk.toFile())


            if (info.flowChunkNumber == info.flowTotalChunks) {
                BufferedOutputStream(
                    processFinalPath(
                        info.destinationPath,
                        info.flowRelativePath,
                    ).toFile()
                        .outputStream()
                ).use { output ->
                    for (chunk in 1..info.flowTotalChunks) {
                        val chunkFile = processTempPath(info.flowIdentifier, chunk).toFile()
                        BufferedInputStream(chunkFile.inputStream()).use {
                            it.copyTo(output)
                        }
                        chunkFile.delete()
                    }
                }

            }
        }

//        file.forEachPart { part ->
//            if(read){
//                throw UnsupportedOperationException("多个part")
//            }
//            if (part is PartData.FileItem) {
//                val fileBytes = part.streamProvider().readBytes()
//                totalSize += fileBytes.size
//                read = true
//            }
//        }
//        return FileManager.postFile(info, totalSize)

    }

    private fun fileListCheck(filePath: Path) {
        if (filePath.toFile().isFile) {
            throw FileException(FileExceptionMsg.INVALID_DIRECTORY_NAME)
        } else if (!filePath.toFile().isDirectory) {
            throw FileException(FileExceptionMsg.DIRECTORY_NOT_FOUND)
        }
    }

    suspend fun getFileList(path: String): List<FileListInfoVO> {
        val filePath = processPath(path)
        fileListCheck(filePath)
        val root = Path(QSetting().type.eq(SettingType.SERVER_ROOT_DIRECTORY).findOne()!!.value)
        val result = mutableListOf<FileListInfoVO>()
        val fileTree = filePath.toFile().walk()
        fileTree.maxDepth(1)
            .filterNot { it.path == filePath.toFile().path }
            .forEach {
                val info = FileListInfoVO(
                    it.name,
                    "/root/${it.relativeTo(root.toFile()).invariantSeparatorsPath}",
                    it.length(), //in bytes
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(it.lastModified()), ZoneId.systemDefault()),
                    it.isDirectory
                )
                result.add(info)
                yield()
            }
        return result
    }

    fun createDirs(path: String) {
        val dirsPath = processPath(path)
        var file = dirsPath.toFile()
        if (!file.isFile && !file.isDirectory) {
            file.mkdirs()
        } else {
            val i = 1
            while (file.isFile) {
                val newPath = "$dirsPath($i++)"
                file = File(newPath)
            }
            file.mkdirs()
        }
    }

    suspend fun renameFile(path: String, name: String) {
        val filePath = processPath(path)
        val file = filePath.toFile()
        if (!file.isFile)
            throw FileException(FileExceptionMsg.FILE_NOT_FOUND)
        name.trim()
        if ("" == name)
            throw FileException(FileExceptionMsg.INVALID_FILE_NAME)
        file.renameTo(filePath.parent.combineSafe(Path("/$name")))
    }

    suspend fun copyFile(path: String, newPath: String) {
        val file = processPath(path).toFile()
        if (!file.exists()) {
            throw FileException(FileExceptionMsg.FILE_NOT_FOUND)
        }
        val newFile = processPath(newPath).toFile()
        if (newFile.exists()) {
            throw FileException(FileExceptionMsg.FILE_ALREADY_EXIST)
        }
        file.copyRecursively(newFile)
    }

    suspend fun deleteFile(path: String) {
        val file = processPath(path).toFile()
        if (!file.exists()) {
            throw FileException(FileExceptionMsg.FILE_NOT_FOUND)
        }
        file.deleteRecursively()
    }

    suspend fun cutFile(path: String, newPath: String) {
        copyFile(path, newPath)
        yield()
        deleteFile(path)
    }

    suspend fun searchFile(path: String, keyword: String): List<FileListInfoVO> {
        val filePath = processPath(path)
        val root = Path(QSetting().type.eq(SettingType.SERVER_ROOT_DIRECTORY).findOne()!!.value)
        val result = mutableListOf<FileListInfoVO>()
        val fileTree = filePath.toFile().walk()
        fileTree.maxDepth(Int.MAX_VALUE)
            .filter { it.name.contains(keyword) }
            .filterNot { it.path == filePath.toFile().path }
            .forEach {
                val info = FileListInfoVO(
                    it.name,
                    "/root/${it.relativeTo(root.toFile()).invariantSeparatorsPath}",
                    it.length(), //in bytes
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(it.lastModified()), ZoneId.systemDefault()),
                    it.isDirectory
                )
                result.add(info)
                yield()
            }
        return result
    }

}