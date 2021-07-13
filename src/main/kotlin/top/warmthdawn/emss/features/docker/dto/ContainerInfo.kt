package top.warmthdawn.emss.features.docker.dto

import top.warmthdawn.emss.features.docker.ContainerStatus

/**
 * @author takanashi
 * @since 2021-07-09
 */

data class ContainerDTO(
    val id: String,         // 容器64位id
    val name: String,       // 容器名称
    val createTime: String, // 创建时间
    val imageId: String,    // 创建容器的镜像64位id
    val status: ContainerStatus?     // 容器状态
)

