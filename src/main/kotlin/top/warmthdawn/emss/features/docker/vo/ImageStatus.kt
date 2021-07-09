package top.warmthdawn.emss.features.docker.vo


data class ImageStatusVO(
    val status: ImageStatus,
    val progress: Double,
)

enum class ImageStatus {
    Ready,
    Downloading,
    Downloaded,
    Unknown
}