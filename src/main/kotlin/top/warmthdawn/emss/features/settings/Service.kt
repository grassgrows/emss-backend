package top.warmthdawn.emss.features.settings

import io.ebean.Database
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.database.entity.Image
import top.warmthdawn.emss.database.entity.Setting
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QImage
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.docker.*
import top.warmthdawn.emss.features.docker.vo.ImageStatus
import top.warmthdawn.emss.features.docker.vo.ImageStatusVO
import top.warmthdawn.emss.features.file.FileService
import top.warmthdawn.emss.features.settings.dto.ImageDTO

/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */

class SettingService(
    private val db: Database,
    private val config: AppConfig,
    val fileService: FileService
) {
    suspend fun getBaseSetting(): BaseSetting {
        val result = QSetting(db).findList().associate { it.type to it.value }
        return BaseSetting(
            result[SettingType.NAME]!!,
            result[SettingType.SERVER_ROOT_DIRECTORY]!!,
            result[SettingType.SERVER_BACKUP_DIRECTORY]!!,
            result[SettingType.TEMPORARY_FOLDER]!!
        )

    }

    suspend fun getImages(): List<Image> {
        return QImage(db).findList()
    }

    suspend fun updateBaseSetting(baseSetting: BaseSetting) {
        if (!baseSetting.name.isNullOrEmpty()) {
            val setting = Setting(SettingType.NAME, baseSetting.name)
            db.update(setting)
        }
        if (!baseSetting.serverRootDirectory.isNullOrEmpty()) {
            val setting = Setting(SettingType.SERVER_ROOT_DIRECTORY, baseSetting.serverRootDirectory)
            setting.update()
            fileService.createDirs(baseSetting.serverRootDirectory)
        }
        if (!baseSetting.serverBackupDirectory.isNullOrEmpty()) {
            val setting = Setting(SettingType.SERVER_ROOT_DIRECTORY, baseSetting.serverBackupDirectory)
            setting.update()
            fileService.createDirs(baseSetting.serverBackupDirectory)
        }
        if (!baseSetting.temporaryFolder.isNullOrEmpty()) {
            val setting = Setting(SettingType.TEMPORARY_FOLDER, baseSetting.temporaryFolder)
            setting.update()
            fileService.createDirs(baseSetting.temporaryFolder)
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


    suspend fun downloadImage(id: Long): Boolean {
        val image = settingService.getImage(id)
        if (!config.testing) {
            return downloadScheduler.startDownload(id, image)
        }
        return true
    }

    suspend fun cancelDownloadImage(id: Long): Boolean {
        if (!config.testing) {
            return downloadScheduler.cancelDownload(id)
        }
        return true
    }

    suspend fun getImageStatus(id: Long): ImageStatusVO {
        if (config.testing) {
            return ImageStatusVO(ImageStatus.Ready)
        }
        val result = downloadScheduler.getStatus(id)

        if (result == null) {
            val image = settingService.getImage(id)
            val status = DockerManager.inspectImage(image.imageId)
            return ImageStatusVO(
                if (status == null) ImageStatus.Ready else ImageStatus.Downloaded
            )
        }
        return result
    }

    suspend fun removeImage(id: Long) {
        if (getImageStatus(id).status != ImageStatus.Downloaded)
            throw ImageException(ImageExceptionMsg.IMAGE_NOT_DOWNLOADED)
        if (QServer().imageId.eq(id).exists())
            throw ImageException(ImageExceptionMsg.IMAGE_REMOVE_WHEN_USED)

        val image = settingService.getImage(id)
        try {
            DockerManager.removeImage(image.imageId)
        } catch (e: Exception) {
            throw ImageException(ImageExceptionMsg.IMAGE_REMOVE_FAILED)
        }

        if (image.canRemove) {
            if(!image.delete())
                throw ImageException(ImageExceptionMsg.IMAGE_DATABASE_REMOVE_FAILED)
        }
    }

}


