package top.warmthdawn.emss.features.file

import ch.qos.logback.core.joran.conditional.IfAction
import io.ktor.util.*
import kotlinx.coroutines.*
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO
import top.warmthdawn.emss.features.file.vo.FileListInfoVO
import top.warmthdawn.emss.features.file.vo.buildVirtualDirectory
import java.io.*
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
            "backup" -> {
                val root = Path(QSetting().type.eq(SettingType.SERVER_BACKUP_DIRECTORY).findOne()!!.value)
                val relativePath = uri.substringAfter("backup")
                return root.combineSafe(Path(relativePath)).toPath()
            }
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
        val tempPath = root.combineSafe(Path(relativePath)).toPath()
        createDirs(tempPath.parent.toString(), false)
        return tempPath
    }

    private fun processFinalPath(destinationPath: String, flowRelativePath: String): Path {
        val path = processPath(destinationPath)
        val relative = Path(flowRelativePath).normalizeAndRelativize()
        if (relative.startsWith("..")) {
            throw PathException(PathExceptionMsg.WRONG_PATH_FORMAT)
        }
        return path.combineSafe(relative).toPath().also {
            createDirs(it.parent.toString(), false)
        }
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
            try {
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
                            if (chunk == info.flowTotalChunks) {
                                chunkFile.parentFile.delete()
                            }
                        }

                    }

                }
            } catch (e: Exception) {
                if (info.flowChunkNumber == info.flowTotalChunks) {
                    val chunkFile = processTempPath(info.flowIdentifier, info.flowChunkNumber).toFile()
                    chunkFile.parentFile.delete()
                }
                throw e
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

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun getFileList(path: String): List<FileListInfoVO> {
        if (path.isEmpty() || path == "/") {
            return buildList {
                buildVirtualDirectory("服务器根目录(root)", "/root")
                buildVirtualDirectory("服务器备份目录(backup)", "/backup")
                QServer().findList().forEach {
                    buildVirtualDirectory(it.name, "/root/${it.location}")
                }
            }

        }
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

    fun createDirs(path: String, process: Boolean = true) {

        val dirsPath = if (process) processPath(path) else Path(path)
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

    // TODO: 2021/7/16 创建文件夹的内部方法（覆盖原文件，不检查路径头） 

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
        file.copyRecursively(newFile, true)
    }

    // TODO: 2021/7/16 复制文件的内部方法（覆盖原文件）

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

    suspend fun readTextFile(path: String, pageNum: Int, pageSize: Int = 1000): String {
        val file = processPath(path).toFile()
        if (!file.exists()) {
            throw FileException(FileExceptionMsg.FILE_NOT_FOUND)
        }
        return withContext(Dispatchers.IO) {
            BufferedReader(InputStreamReader(file.inputStream())).use {
                runCatching {
                    if (it.skip(pageNum * pageSize.toLong()) < 0) {
                        ""
                    } else {
                        val buffer = CharArray(pageSize)
                        val read = it.read(buffer, 0, buffer.size)
                        if (read > 0)
                            String(buffer, 0, read)
                        else
                            ""
                    }
                }.getOrElse { "" }
            }
        }

    }

    suspend fun saveTextFile(path: String, text: String) {
        File(path).writeText(text)
        //TODO: 有机会改成服务器设置
        if (File(path).length() > 1024 * 1024)
            throw FileException(FileExceptionMsg.FILE_SIZE_TOO_LARGE)
    }

    fun innerCreateDirs(path: String) {
        val dirsPath = Path(path)
        val file = dirsPath.toFile()
        if (!file.isFile && !file.isDirectory) {
            file.mkdirs()
        }
    }

    fun findDuplicateFiles(path: String, newPath: String): Int {
        val file = processPath(path).toFile()
        if (!file.exists()) {
            throw FileException(FileExceptionMsg.FILE_NOT_FOUND)
        }
        val newFile = processPath(newPath).toFile()
        if (newFile.exists()) {
            if (newFile.isDirectory && newFile.length() == 0L) {
                return 1
            } else if (newFile.isFile) {
                return 1
            }
            val fileTree = file.walk()
            val files = mutableListOf<String>()
            fileTree.maxDepth(Int.MAX_VALUE)
                .filterNot { it.path == file.path }
                .forEach {
                    files.add(it.name)
                }
            val newfileTree = newFile.walk()
            val newfiles = mutableListOf<String>()
            newfileTree.maxDepth(Int.MAX_VALUE)
                .filterNot { it.path == newFile.path }
                .forEach {
                    newfiles.add(it.name)
                }
            return files.intersect(newfiles).size
        }
        return 0
    }


}