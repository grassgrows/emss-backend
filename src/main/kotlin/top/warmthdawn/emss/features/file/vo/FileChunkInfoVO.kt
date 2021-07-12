package top.warmthdawn.emss.features.file.vo

/**
 *
 * @author sunday7994
 * @date 2021/7/12
 */

data class FileChunkInfoVO(
    val fileChunkStatus: FileChunkStatus,
    val filename: String?,
    val identifier: String?,
    val finalPath: String?,
    )
