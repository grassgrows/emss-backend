package top.warmthdawn.emss.features.settings

import org.ktorm.database.Database
import org.ktorm.dsl.*
import top.warmthdawn.emss.database.entity.*

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */

class SettingService(
    val db: Database
) {
    suspend fun getBaseSetting(): BaseSetting {
        val result = db.from(Settings).select().associate { it[Settings.type] to it[Settings.value] }
        return BaseSetting(result[SettingType.Name]!!, result[SettingType.ServerRootDirectory]!!)
    }

    suspend fun getDockerImage(): List<Image> {
        return db.from(Images)
            .select()
            .map { Images.createEntity(it) }
    }
}

class ServerService(
    val db: Database
){
    suspend fun getServerInfo(): List<Server>{
        return db.from(Servers)
            .select()
            .map{Servers.createEntity(it)}
    }
}