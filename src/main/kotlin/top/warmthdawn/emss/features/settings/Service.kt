package top.warmthdawn.emss.features.settings

import io.ktor.application.*
import org.koin.java.KoinJavaComponent.inject
import top.warmthdawn.emss.database.DatabaseFactory

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */

class SettingService(
    val db: DatabaseFactory
) {
    suspend fun getBaseSetting(): BaseSetting {
        TODO("未完成")
    }
}