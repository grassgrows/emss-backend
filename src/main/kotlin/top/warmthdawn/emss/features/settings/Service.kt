package top.warmthdawn.emss.features.settings

import io.ebean.Database
import top.warmthdawn.emss.database.entity.Image
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QImage
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QSetting

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */

class SettingService(
    val db: Database
) {
    suspend fun getBaseSetting(): BaseSetting {
//        val result = db.from(Settings).select().associate { it[Settings.type] to it[Settings.value] }
        val result = QSetting(db).findList().associate { it.type to it.value }
        return BaseSetting(result[SettingType.Name]!!, result[SettingType.ServerRootDirectory]!!)

    }

    suspend fun getDockerImage(): List<Image> {
//        return db.from(Images)
//            .select()
//            .map { Images.createEntity(it) }
        return QImage(db).findList()
    }

class ServerService(
    val db: Database
) {
    suspend fun getServerInfo(): List<Server> {
        return QServer(db).findList()
    }
}


