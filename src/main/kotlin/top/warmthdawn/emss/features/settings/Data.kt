package top.warmthdawn.emss.features.settings

import org.junit.rules.TemporaryFolder


/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */

data class BaseSetting(
    val name: String? = null,
    val serverRootDirectory: String? = null,
    val temporaryFolder: String? = null,
)
