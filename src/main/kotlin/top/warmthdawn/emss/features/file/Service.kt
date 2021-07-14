package top.warmthdawn.emss.features.file

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO
import top.warmthdawn.emss.features.file.vo.FileChunkInfoVO
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


/**
 *
 * @author sunday7994
 * @date 2021/7/12
 */

class FileService {

    fun processPath(input: String): Path {

        var uri = URI(input).normalize().path

        if (uri == "/" || uri == "") {
            uri = "/root/"
        }

        uri = if (uri.startsWith("/")) uri.substring(1) else uri

        when (uri.substringBefore('/')) {
            "root" -> {
                val root = Path(QSetting().type.eq(SettingType.ServerRootDirectory).findOne()!!.value)
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

    suspend fun getFileInfo(info: FileChunkInfoDTO): FileChunkInfoVO {
        TODO("xxxx")
//        return FileManager.getFile(info)
    }

    private val objectMapper = ObjectMapper()

    val DEFAULT_BUFFER_SIZE = 1024 * 8
    suspend fun uploadFile(input: InputStream, info: FileChunkInfoDTO) {
        val finalPath = "${info.destinationPath}/${info.flowRelativePath}-${info.flowChunkNumber}"
        val filePath = processPath(finalPath)


        withContext(Dispatchers.IO) {
            File(filePath.toString()).outputStream().use { out ->
                var transferred = input.copyTo(out)
            }
            if (info.flowChunkNumber == info.flowTotalChunks) {
                BufferedOutputStream(processPath("${info.destinationPath}/${info.flowRelativePath}").toFile().outputStream()).use { output ->
                    for (chunk in 1..info.flowTotalChunks) {
                        val chunkFile =
                            processPath("${info.destinationPath}/${info.flowRelativePath}-${chunk}").toFile()
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

    suspend fun getFileList(path: String): List<FileListInfoVO> {
        val filePath: Path = processPath(path)
        val root = Path(QSetting().type.eq(SettingType.ServerRootDirectory).findOne()!!.value)
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

    suspend fun createDirs(path: String) {
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
        val newFile = processPath(newPath).toFile()
        file.copyRecursively(newFile)
    }

    suspend fun deleteFile(path: String) {
        val file = processPath(path).toFile()
        file.deleteRecursively()
    }

    suspend fun cutFile(path: String, newPath: String) {
        copyFile(path, newPath)
        yield()
        deleteFile(path)
    }

    suspend fun searchFile(path: String, keyword: String): List<FileListInfoVO> {
        val filePath = processPath(path)
        val root = Path(QSetting().type.eq(SettingType.ServerRootDirectory).findOne()!!.value)
        val result = mutableListOf<FileListInfoVO>()
        val fileTree = filePath.toFile().walk()
        fileTree.maxDepth(Int.MAX_VALUE)
            .filter { it.name.contains(keyword) }
            .forEach {
                val info = FileListInfoVO(
                    it.name,
                    "/root/${it.toRelativeString(root.toFile())}",
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