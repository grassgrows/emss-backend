package top.warmthdawn.emss.features.settings

import kotlinx.serialization.Serializable

/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */

@Serializable
data class BaseSetting(
    val name: String? = null,
    val serverRootDirectory: String? = null,
)
