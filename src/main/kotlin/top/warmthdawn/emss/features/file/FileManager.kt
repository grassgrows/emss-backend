package top.warmthdawn.emss.features.file

import io.ktor.util.*
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO
import top.warmthdawn.emss.features.file.vo.FileChunkInfoVO
import top.warmthdawn.emss.features.file.vo.FileChunkStatus
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.math.floor


/**
 *
 * @author sunday7994
 * @date 2021/7/12
 */
object FileManager{
    val temporaryFolder: String = ""
    val maxFileSize: Int = -1

    init {
        TODO("新建文件夹")
    }

    private fun cleanIdentifier(identifier: String): String {
        val re = Regex("[^0-9A-Za-z_-]/g")
        return re.replace(identifier, "")
    }

    private fun getChunkFilename(chunkNumber: Int, identifier: String): Path {
        val identifierCleaned = cleanIdentifier(identifier)
        return Path(temporaryFolder, "./uploader-$identifierCleaned.$chunkNumber")
    }

    private fun validateRequest(info: FileChunkInfoDTO, fileSize: Int? = null): FileChunkStatus {
        val identifier = cleanIdentifier(info.identifier)

        // 检查该请求是否合理
        if (info.chunkNumber == 0 ||
            info.chunkSize == 0 ||
            info.totalSize == 0 ||
            identifier.isEmpty() ||
            info.filename.isEmpty()
        ) {
            return FileChunkStatus.NON_UPLOADER_REQUEST
        }
        val numberOfChunks = floor(info.totalSize / (info.chunkSize * 1.0)).toInt().coerceAtLeast(1)
        if (info.chunkNumber > numberOfChunks) {
            return FileChunkStatus.INVALID_UPLOADER_REQUEST1
        }

        // 检查上传文件大小是否超出系统限制
        if (maxFileSize > 0 && info.totalSize > maxFileSize) {
            return FileChunkStatus.INVALID_UPLOADER_REQUEST2
        }

        if (fileSize != null) {
            if (info.chunkNumber < numberOfChunks && fileSize != info.chunkSize) {
                // 上传的文件块大小出错
                return FileChunkStatus.INVALID_UPLOADER_REQUEST3
            }
            if (numberOfChunks > 1 &&
                info.chunkNumber == numberOfChunks &&
                fileSize != ((info.totalSize % info.chunkSize) + info.chunkSize)
            ) {
                // 最后一个文件块大小出错（最后一块的大小可能比其他文件块的大小要更大）
                return FileChunkStatus.INVALID_UPLOADER_REQUEST4
            }
            if (numberOfChunks == 1 && fileSize != info.totalSize) {
                // 单文件块大小出错
                return FileChunkStatus.INVALID_UPLOADER_REQUEST5
            }
        }
        return FileChunkStatus.VALID
    }

    fun getFile(info: FileChunkInfoDTO): FileChunkInfoVO {

        if (validateRequest(info) == FileChunkStatus.VALID) {
            val chunkFilename = getChunkFilename(info.chunkNumber, info.identifier)
            return if (chunkFilename.toFile().exists()) {
                FileChunkInfoVO(
                    FileChunkStatus.FOUND,
                    info.filename,
                    info.identifier,
                    info.finalPath
                )
            } else {
                FileChunkInfoVO(
                    FileChunkStatus.NOT_FOUND,
                    null,
                    null,
                    null
                )
            }
        } else {
            return FileChunkInfoVO(
                FileChunkStatus.NOT_FOUND,
                null,
                null,
                null
            )
        }
    }

    //'partly_done', filename, original_filename, identifier
//'done', filename, original_filename, identifier
//'invalid_uploader_request', null, null, null
//'non_uploader_request', null, null, null
    fun postFile(info: FileChunkInfoDTO, fileSize: Int?): FileChunkInfoVO {

        val identifier = cleanIdentifier(info.identifier)

        val validation =
            validateRequest(info)
        if (validation == FileChunkStatus.VALID) {
            var chunkFilename = getChunkFilename(info.chunkNumber, identifier)

            val numberOfChunks = floor(info.totalSize / (info.chunkSize * 1.0)).toInt().coerceAtLeast(1)

            val result = (1..numberOfChunks)
                .map {
                    getChunkFilename(it, identifier)
                }
                .all {
                    it.toFile().exists()
                }

            if (result) {
                validateRequest(info, fileSize)
                combineChunks(info)
                return FileChunkInfoVO(
                    FileChunkStatus.DONE,
                    info.filename,
                    info.identifier,
                    info.finalPath
                )
            } else {
                return FileChunkInfoVO(
                    FileChunkStatus.PARTLY_DONE,
                    info.filename,
                    info.identifier,
                    info.finalPath
                )
            }

        } else {
            return FileChunkInfoVO(
                validation,
                info.filename,
                info.identifier,
                info.finalPath
            )
        }
    }

    private fun combineChunks(info: FileChunkInfoDTO) {
        val numberOfChunks = floor(info.totalSize / (info.chunkSize * 1.0)).toInt().coerceAtLeast(1)
        BufferedOutputStream(File(info.finalPath).outputStream()).use { output ->
            for (chunk in 1..numberOfChunks) {
                val chunkFile = getChunkFilename(chunk, info.identifier).toFile()
                BufferedInputStream(chunkFile.inputStream()).use {
                    it.copyTo(output)
                }
                chunkFile.delete()
            }
        }
    }

    fun processPath(input: String): Path{

        var uri = URI(input).normalize().path

        if (uri == "/") {
            uri = "/root"
        }

        uri = if (uri.startsWith("/")) uri.substring(1) else uri

        when (val type = uri.substringBefore('/')) {
            "root" -> {
                val root = Path(QSetting().type.eq(SettingType.ServerRootDirectory).findOne()!!.value)
                val relativePath = type.substringAfter("root/")
                //用户权限
                val serverLocations = arrayOf("sky/et2", "timw4")
                if (serverLocations.any { relativePath.startsWith(it) }) {
                    return root.combineSafe(Path(relativePath)).toPath()
                } else {
                    throw IllegalAccessException("Insufficient permission level")
                }
            }
//            "backup" -> {
//
//            }
            else -> {
                throw IllegalAccessException("Path format error")
            }
        }
    }

    fun getFiles(input: String): Sequence<File> {
        val filePath = processPath(input)
        val fileTree: FileTreeWalk = File(filePath.toString()).walk()
        return fileTree.maxDepth(1)
    }


//    fun getALLFiles(input: String): List<File> {
//        val filePath = FileManager.processPath(input)
//        val fileTree: FileTreeWalk = File(filePath.toString()).walk()
//        return fileTree.maxDepth(Int.MAX_VALUE)
//            .asIterable().toList()
//    } 不确定是否需要这个方法


}