package top.warmthdawn.emss.features.docker

/**
 * @author takanashi
 * @since 2021-07-10
 */

enum class ContainerStatus {
    Running,
    Stopped,
    Unknown,
    Removed,
    /*
    Created,
    Running,
    Paused,
    Restarting,
    Removing,
    Exited,
    Dead
    */
}