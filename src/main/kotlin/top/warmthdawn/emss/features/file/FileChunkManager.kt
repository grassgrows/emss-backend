package top.warmthdawn.emss.features.file

import top.warmthdawn.emss.features.file.dto.FileChunkInfoDTO


/**
 *
 * @author sunday7994
 * @date 2021/7/12
 */
object FileChunkManager {
    private const val maxFileSize: Int = -1

    private fun cleanIdentifier(flowIdentifier: String): String {
        val re = Regex("[^0-9A-Za-z_-]/g")
        return re.replace(flowIdentifier, "")
    }


    fun validateRequest(info: FileChunkInfoDTO) {

        val flowIdentifier = cleanIdentifier(info.flowIdentifier)

        // 检查该请求是否合理
        if (info.flowChunkNumber == 0 ||
            info.flowChunkSize == 0 ||
            info.flowTotalSize == 0 ||
            flowIdentifier.isEmpty() ||
            info.flowFilename.isEmpty()
        ) {
            throw FileChunkException(FileChunkExceptionMsg.NON_UPLOADER_REQUEST)
        }

        if (info.flowChunkNumber > info.flowTotalChunks) {
            throw FileChunkException(FileChunkExceptionMsg.INVALID_UPLOADER_REQUEST1)
        }

        // 检查上传文件大小是否超出系统限制
        if (maxFileSize > 0 && info.flowTotalSize > maxFileSize) {
            throw FileChunkException(FileChunkExceptionMsg.INVALID_UPLOADER_REQUEST2)
        }


    }

    fun fileChunkCheck(info: FileChunkInfoDTO, fileSize: Int): Boolean {

        if (info.flowChunkNumber < info.flowTotalChunks && fileSize != info.flowChunkSize) {
            // 上传的文件块大小出错
            throw FileChunkException(FileChunkExceptionMsg.INVALID_UPLOADER_REQUEST3)
        }
        if (info.flowTotalChunks > 1 &&
            info.flowChunkNumber == info.flowTotalChunks &&
            fileSize != ((info.flowTotalSize % info.flowChunkSize) + info.flowChunkSize)
        ) {
            // 最后一个文件块大小出错（最后一块的大小可能比其他文件块的大小要更大）
            throw FileChunkException(FileChunkExceptionMsg.INVALID_UPLOADER_REQUEST4)
        }
        if (info.flowTotalChunks == 1 && fileSize != info.flowTotalSize) {
            // 单文件块大小出错
            throw FileChunkException(FileChunkExceptionMsg.INVALID_UPLOADER_REQUEST5)
        }
        return true
    }
}
