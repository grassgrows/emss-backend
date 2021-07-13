package top.warmthdawn.emss.features.file

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.content.*
import kotlinx.coroutines.yield
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO
import top.warmthdawn.emss.features.file.vo.FileChunkInfoVO
import top.warmthdawn.emss.features.file.vo.FilesInfoVO
import java.io.File
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
    suspend fun getFileInfo(info: FileChunkInfoDTO): FileChunkInfoVO {
        return FileManager.getFile(info)
    }

    private val objectMapper = ObjectMapper()

    suspend fun uploadFile(file: MultiPartData): FileChunkInfoVO {
        lateinit var info: FileChunkInfoDTO
        val filePath = FileManager.processPath(info.finalPath)

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

    suspend fun getFileList(input: String): List<FilesInfoVO> {
        val root = Path(QSetting().type.eq(SettingType.ServerRootDirectory).findOne()!!.value)
        val result: MutableList<FilesInfoVO> = mutableListOf()
        FileManager.getFiles(input).forEach {
            val info = FilesInfoVO(
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


}