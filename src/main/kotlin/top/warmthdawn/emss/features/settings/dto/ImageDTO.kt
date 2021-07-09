package top.warmthdawn.emss.features.settings.dto

import javax.management.loading.ClassLoaderRepository

data class ImageDTO(
    val name: String,
    val repository: String,
    val tag: String,
)