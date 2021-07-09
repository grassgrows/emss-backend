package top.warmthdawn.emss.features.settings

import io.ebean.Database
import top.warmthdawn.emss.database.entity.Image
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.Setting
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QImage
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.settings.dto.ImageDTO

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */

class SettingService(
    val db: Database
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
            db.save(setting)
        }
        if (!baseSetting.serverRootDirectory.isNullOrEmpty()) {
            val setting = Setting(SettingType.ServerRootDirectory, baseSetting.serverRootDirectory)
            setting.save()
        }
    }

    suspend fun getImage(id: Long): Image {
        return QImage(db).id.eq(id).findOne()!!
    }

    suspend fun createImage(imageDTO: ImageDTO) {
        val image = Image(
            name = imageDTO.name,
            repository = imageDTO.repository,
            tag = imageDTO.tag
        )
        image.save()
    }
}


