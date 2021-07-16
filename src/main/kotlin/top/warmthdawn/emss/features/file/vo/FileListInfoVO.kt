package top.warmthdawn.emss.features.file.vo

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

fun MutableList<FileListInfoVO>.buildVirtualDirectory(name: String, path: String): FileListInfoVO {
    return FileListInfoVO(
        fileName = name,
        filePath = path,
        size = 0,
        lastModified = LocalDateTime.now(),
        isDirectory = true
    ).also {
        this.add(it)
    }
}