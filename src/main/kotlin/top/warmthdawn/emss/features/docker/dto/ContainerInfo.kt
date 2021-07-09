package top.warmthdawn.emss.features.docker.dto

data class ContainerInfo(
    val id: String,         // 容器唯一id
    val name: String,       // 容器名称
    val createTime: String, // 创建时间
    val imageId: String,    // 创建容器的镜像id
    val status: String?     // 容器状态("created" "running" "paused" "restarting" "removing" "exited" "dead")
)

