package top.warmthdawn.emss.features.file.dto

import io.ktor.locations.*

/**
 *
 * @author sunday7994
 * @date 2021/7/12
 */

@OptIn(KtorExperimentalLocationsAPI::class)
@Location("/file/upload")
data class FileChunkInfoDTO (
    val flowChunkNumber: Int,
    val flowTotalChunks: Int,
    val flowChunkSize: Int,
    val flowTotalSize: Int,
    val flowIdentifier: String,
    val flowFilename: String,
    val flowRelativePath: String,
    val destinationPath: String,
)