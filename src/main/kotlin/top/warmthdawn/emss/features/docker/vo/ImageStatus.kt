package top.warmthdawn.emss.features.docker.vo

import kotlinx.serialization.Serializable


@Serializable
data class ImageStatusVO(
    val status: ImageStatus,
    val progress: Double = 0.0,
    val speed: Double = 0.0,
)

@Serializable
enum class ImageStatus {
    Ready,
    Downloading,
    Downloaded,
    Failed,
    Unknown
}