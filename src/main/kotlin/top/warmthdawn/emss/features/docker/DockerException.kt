package top.warmthdawn.emss.features.docker

/**
 * @author takanashi
 * @since 2021-07-13
 */

open class DockerException(
    private val msg: String
):Exception(msg)

class ImageNotFoundException(
    private val msg: String
):DockerException(msg)

class ImageNotDownloadedException(
    private val msg: String
):DockerException(msg)

class ImageRemoveFailedException(
    private val msg: String
):DockerException(msg)

class ImageRemoveWhenUsedException(
    private val msg: String
):DockerException(msg)

