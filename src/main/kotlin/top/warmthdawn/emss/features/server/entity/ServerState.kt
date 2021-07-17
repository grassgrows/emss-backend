package top.warmthdawn.emss.features.server.entity

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
enum class ServerState {
    INITIALIZE, //需要初始化： 服务器刚创建或修改设置的时候，此状态需要删除容器重新创建
    STARTING, //正在启动服务器
    RUNNING, //运行状态
    STOPPING, //正在关闭服务器状态：必须经历本状态服务器才算正常关闭，同时强制关闭也仅再本状态有效
    STOPPED, //服务器已经关闭
}