package top.warmthdawn.emss.features.settings

import io.ebean.Database
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.database.entity.Image
import top.warmthdawn.emss.database.entity.Setting
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QImage
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.docker.ImageDownloadScheduler
import top.warmthdawn.emss.features.docker.vo.ImageStatus
import top.warmthdawn.emss.features.docker.vo.ImageStatusVO
import top.warmthdawn.emss.features.settings.dto.ImageDTO

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */

class SettingService(
    private val db: Database,
    private val config: AppConfig
) {
    suspend fun getBaseSetting(): BaseSetting {
        val result = QSetting(db).findList().associate { it.type to it.value }
        return BaseSetting(result[SettingType.Name]!!, result[SettingType.ServerRootDirectory]!!)

    }

    suspend fun getImages(): List<Image> {
        return QImage(db).findList()
    }

    suspend fun updateBaseSetting(baseSetting: BaseSetting) {
        if (!baseSetting.name.isNullOrEmpty()) {
            val setting = Setting(SettingType.Name, baseSetting.name)
            db.update(setting)
        }
        if (!baseSetting.serverRootDirectory.isNullOrEmpty()) {
            val setting = Setting(SettingType.ServerRootDirectory, baseSetting.serverRootDirectory)
            setting.update()
        }
    }

    suspend fun getImage(id: Long): Image {
        return QImage(db).id.eq(id).findOne()!!
    }

    suspend fun createImage(imageDTO: ImageDTO) {
        val image = Image(
            name = imageDTO.name,
            repository = imageDTO.repository,
            tag = imageDTO.tag ?: ""
        )
        image.insert()
    }
}

class ImageService(
    private val settingService: SettingService,
    private val downloadScheduler: ImageDownloadScheduler,
    private val config: AppConfig
) {


    suspend fun downloadImage(id: Long) {
        val image = settingService.getImage(id)
        if (!config.testing) {
            downloadScheduler.startDownload(id, image)
        }
    }

    suspend fun getImageStatus(id: Long): ImageStatusVO {
        if (config.testing) {
            return ImageStatusVO(ImageStatus.Ready)
        }
        val result = downloadScheduler.getStatus(id)

        if (result == null) {
            val image = settingService.getImage(id)
            if (image.imageId == null) {
                return ImageStatusVO(ImageStatus.Ready)
            }
            val status = TODO("查询不处于下载状态的Image")
            return ImageStatusVO(status)
        }
        return result
    }

}


