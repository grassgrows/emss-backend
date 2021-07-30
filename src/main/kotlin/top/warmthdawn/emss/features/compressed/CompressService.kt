package top.warmthdawn.emss.features.compressed

import io.ktor.util.*
import org.apache.commons.compress.archivers.zip.ZipFile
import top.warmthdawn.emss.features.file.FileException
import top.warmthdawn.emss.features.file.FileExceptionMsg
import top.warmthdawn.emss.features.file.FileService
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
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
        if (file.isFile && file.nameWithoutExtension.endsWith(".zip")) {
            val path = "${file.path}${File.pathSeparator}解压文件_${file.name}"
            //判断要解压的目录是否存在
            if (fileService.processPath(path).toFile().exists()) {
                throw FileException(FileExceptionMsg.FILE_ALREADY_EXIST)
            }

            var outputStream: OutputStream? = null
            var inputStream: InputStream? = null
            try {
                val zipFile = ZipFile(file)
                val entries = zipFile.entries
                val buffer = ByteArray(1024)


                while (entries.hasMoreElements()) {

                    val zipEntry = entries.nextElement()
                    val zipEntryName = zipEntry.name

                    inputStream = zipFile.getInputStream(zipEntry)

                    val descFilePath = path + File.pathSeparator + zipEntryName
                    val descFile = File(descFilePath)
                    val descParent = descFile.parentFile
                    if (!descParent.exists()) {
                        descParent.mkdirs()
                    }
                    if (!descFile.createNewFile()) throw FileException(FileExceptionMsg.FILE_ALREADY_EXIST)
                    outputStream = FileOutputStream(descFile)

                    var len: Int
                    while (inputStream.read(buffer).also { len = it } > 0) {
                        outputStream.write(buffer, 0, len)
                    }
                    inputStream.close()
                    outputStream.close()

                }
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } else throw FileException(FileExceptionMsg.FILE_TYPE_UN_SUPPORT)
        //TODO: 可能可以使用(未测试

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
