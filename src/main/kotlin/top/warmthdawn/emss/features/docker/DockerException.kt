package top.warmthdawn.emss.features.docker

/**
 * @author takanashi
 * @since 2021-07-15
 */

class ImageException(
    val imageExceptionMsg: ImageExceptionMsg
) : Exception(imageExceptionMsg.toString())

enum class ImageExceptionMsg {
    IMAGE_NOT_FOUND,
    IMAGE_NOT_DOWNLOADED,
    IMAGE_REMOVE_FAILED,
    IMAGE_DATABASE_REMOVE_FAILED,
    IMAGE_REMOVE_WHEN_USED
}

class ContainerException(
    val containerExceptionMsg: ContainerExceptionMsg
) : Exception(containerExceptionMsg.toString())

enum class ContainerExceptionMsg {
    CONTAINER_GET_INFO_FAILED,
    CONTAINER_NOT_FOUND,
}



