package top.warmthdawn.emss.features.file.vo

import java.time.LocalDateTime


/**
 *
 * @author sunday7994
 * @date 2021/7/13
 */

data class FilesInfoVO(
    val fileName: String,
    val filePath: String,
    val size: Long,
    val lastModified: LocalDateTime,
)