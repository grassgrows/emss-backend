package top.warmthdawn.emss.features.file

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.content.*
import io.ktor.util.*
import kotlinx.coroutines.yield
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO
import top.warmthdawn.emss.features.file.vo.FileChunkInfoVO
import top.warmthdawn.emss.features.file.vo.FileListInfoVO
import java.io.File
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
        return FileManager.getFile(info)
    }

    private val objectMapper = ObjectMapper()

    suspend fun uploadFile(file: MultiPartData): FileChunkInfoVO {
        lateinit var info: FileChunkInfoDTO
        val filePath = processPath(info.finalPath)

        var totalSize = 0
        file.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    info = objectMapper.readValue(part.value) //需验证
                }
                is PartData.FileItem -> {
                    val fileBytes = part.streamProvider().readBytes()
                    totalSize += fileBytes.size
                    File(filePath.toString()).writeBytes(fileBytes)
                }
            }
        }
        return FileManager.postFile(info, totalSize)
    }

    suspend fun getFileList(input: String): List<FileListInfoVO> {
        val filePath: Path = processPath(input)
        val root = Path(QSetting().type.eq(SettingType.ServerRootDirectory).findOne()!!.value)
        val result: MutableList<FileListInfoVO> = mutableListOf()
        FileManager.getFiles(filePath).forEach {
            val info = FileListInfoVO(
                it.name,
                root.relativize(it.toPath()).toString(),
                it.length(), //in bytes
                LocalDateTime.ofInstant(Instant.ofEpochMilli(it.lastModified()), ZoneId.systemDefault())
            )
            result.add(info)
            yield()
        }
        return result
    }

    suspend fun createDirs(input: String){
        val dirsPath = processPath(input)
        FileManager.createDirs(dirsPath)
    }

    suspend fun renameFile(input: String){
        val file = File(processPath(input).toString())
    }

}