package top.warmthdawn.emss.features.docker.vo

/**
 * @author takanashi
 * @since 2021-07-10
 */

enum class ContainerStatus {
    Created,
    Running,
    Paused,
    Restarting,
    Removing,
    Exited,
    Dead
}