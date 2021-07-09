package top.warmthdawn.emss.features.settings

import top.warmthdawn.emss.database.entity.Image
import top.warmthdawn.emss.database.entity.User

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */

data class BaseSetting(
    val name: String?,
    val serverRootDirectory: String?,
)
