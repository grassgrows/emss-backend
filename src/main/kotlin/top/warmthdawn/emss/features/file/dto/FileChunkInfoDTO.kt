package top.warmthdawn.emss.features.file.dto

/**
 *
 * @author sunday7994
 * @date 2021/7/12
 */

data class FileChunkInfoDTO (
    val chunkNumber: Int,
    val chunkSize: Int,
    val totalSize: Int,
    val identifier: String,
    val filename: String,
    val finalPath: String,
)