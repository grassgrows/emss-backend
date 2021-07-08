package top.warmthdawn.emss.features.settings

import io.ktor.application.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import top.warmthdawn.emss.database.DatabaseFactory
import top.warmthdawn.emss.database.entity.Image
import top.warmthdawn.emss.database.entity.Images
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.Settings

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