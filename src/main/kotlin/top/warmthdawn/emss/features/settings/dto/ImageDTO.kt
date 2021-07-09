package top.warmthdawn.emss.features.settings.dto

import kotlinx.serialization.Serializable
import javax.management.loading.ClassLoaderRepository

@Serializable
data class ImageDTO(
    val name: String,
    val repository: String,
    val tag: String? = null,
)