package top.warmthdawn.emss.features.file

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ebean.Database
import io.ktor.http.content.*
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO
import top.warmthdawn.emss.features.file.vo.FileChunkInfoVO
import java.io.File


/**
 *
 * @author sunday7994
 * @date 2021/7/12
 */

class FileService(
    private val db: Database,

    ) {
    suspend fun getFileInfo(info: FileChunkInfoDTO): FileChunkInfoVO {
        return FileManager.getFile(info)
    }
    val objectMapper = ObjectMapper()

    suspend fun uploadFile(file: MultiPartData):FileChunkInfoVO{
        lateinit var info: FileChunkInfoDTO
        var fileSize: Int
        val filePath = QSetting(db).type.eq(SettingType.ServerRootDirectory).findOne()!!.value + info.finalPath

        var totalSize = 0
        file.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    info = objectMapper.readValue(part.value)
                }
                is PartData.FileItem -> {
                    val fileBytes = part.streamProvider().readBytes()
                    totalSize+= fileBytes.size
                    File(filePath).writeBytes(fileBytes)
                    TODO("filePath路径检测")
                }
            }
        }
        return FileManager.postFile(info, totalSize)
    }
}