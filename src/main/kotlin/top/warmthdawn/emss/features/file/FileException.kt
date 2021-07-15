package top.warmthdawn.emss.features.file


/**
 *
 * @author sunday7994
 * @date 2021/7/13
 */

class PathException(
    val pathExceptionMsg: PathExceptionMsg
) : Exception(pathExceptionMsg.toString())

enum class PathExceptionMsg {
    INSUFFICIENT_PERMISSION_LEVEL,
    WRONG_PATH_FORMAT
}

class FileException(
    val fileExceptionMsg: FileExceptionMsg
) : Exception(fileExceptionMsg.toString())

enum class FileExceptionMsg {
    FILE_NOT_FOUND,
    INVALID_FILE_NAME
}

class FileChunkException(
    val fileChunkExceptionMsg: FileChunkExceptionMsg
) : Exception(fileChunkExceptionMsg.toString())

enum class FileChunkExceptionMsg {
    NON_UPLOADER_REQUEST,
    INVALID_UPLOADER_REQUEST1,
    INVALID_UPLOADER_REQUEST2,
    INVALID_UPLOADER_REQUEST3,
    INVALID_UPLOADER_REQUEST4,
    INVALID_UPLOADER_REQUEST5,
}