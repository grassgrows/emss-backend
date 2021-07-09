package top.warmthdawn.emss.features.settings

import kotlinx.serialization.Serializable
import top.warmthdawn.emss.database.entity.Image
import top.warmthdawn.emss.database.entity.User

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */

@Serializable
data class BaseSetting(
    val name: String? = null,
    val serverRootDirectory: String? = null,
)
