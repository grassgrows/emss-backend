package top.warmthdawn.emss.features.compressed

import io.ktor.util.*
import top.warmthdawn.emss.features.file.FileException
import top.warmthdawn.emss.features.file.FileExceptionMsg
import top.warmthdawn.emss.features.file.FileService
import java.io.File
import kotlin.io.path.Path

/**
 *
 * @author WarmthDawn
 * @since 2021-07-27
 */
class CompressService(
    private val fileService: FileService
) {

    suspend fun compressFile(paths: Array<String>) {
        val files = paths.map {
            fileService.processPath(it).toFile()
        }
        files.forEach {
            if (!it.exists())
                throw FileException(FileExceptionMsg.FILE_NOT_FOUND)
        }
        compressFile(files)
    }
    //压缩
    private fun compressFile(files: List<File>) {
        //TODO: 小符号加油
    }

    suspend fun uncompressFile(path: String) {
        val filePath = fileService.processPath(path)
        val file = filePath.toFile()
        if (!file.exists())
            throw FileException(FileExceptionMsg.FILE_NOT_FOUND)
        uncompressFile(file)
    }

    //解压缩
    private fun uncompressFile(file: File) {
        //TODO: 小符号加油
    }

    //压缩/解压的进度
    fun getWorkingProgress(): List<CompressProgress> {
        //TODO: 小符号加油（记得带锁）
        return emptyList()
    }
}

data class CompressProgress(
    val fileName: String,
    val totalProgress: Double,
)