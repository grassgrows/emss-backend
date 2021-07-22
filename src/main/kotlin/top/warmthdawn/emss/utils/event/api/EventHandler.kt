package top.warmthdawn.emss.utils.event.api

/**
 * 事件处理器
 * @author WarmthDawn
 * @since 2021-05-13
 */
fun interface EventHandler<in T> {
    suspend fun handle(args: T)
}