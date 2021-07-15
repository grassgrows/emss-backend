package top.warmthdawn.emss.features.docker

/**
 * @author takanashi
 * @since 2021-07-13
 */

class ImageException(
    val imageExceptionMsg: ImageExceptionMsg
) : Exception(imageExceptionMsg.toString())

enum class ImageExceptionMsg {
    IMAGE_NOT_FOUND,
    IMAGE_NOT_DOWNLOADED,
    IMAGE_REMOVE_FAILED,
    IMAGE_REMOVE_WHEN_USED
}


