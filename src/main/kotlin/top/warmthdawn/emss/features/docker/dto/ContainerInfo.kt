package top.warmthdawn.emss.features.docker.dto

import top.warmthdawn.emss.features.docker.vo.ContainerStatus

/**
 * @author takanashi
 * @since 2021-07-09
 */

data class ContainerInfo(
    val id: String,         // 容器唯一id
    val name: String,       // 容器名称
    val createTime: String, // 创建时间
    val imageId: String,    // 创建容器的镜像id
    val status: ContainerStatus?     // 容器状态
)

