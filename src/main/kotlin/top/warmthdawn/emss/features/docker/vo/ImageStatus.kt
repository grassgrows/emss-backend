package top.warmthdawn.emss.features.docker.vo


data class ImageStatusVO(
    val status: ImageStatus,
    val progress: Double = 0.0,
    val speed: Double = 0.0,
)

enum class ImageStatus {
    Ready,
    Downloading,
    Downloaded,
    Failed,
}