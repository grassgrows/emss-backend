package top.warmthdawn.emss.features.file.vo

import java.nio.file.ClosedDirectoryStreamException
import java.time.LocalDateTime


/**
 *
 * @author sunday7994
 * @date 2021/7/13
 */

data class FileListInfoVO(
    val fileName: String,
    val filePath: String,
    val size: Long,
    val lastModified: LocalDateTime,
    val isDirectory: Boolean
)