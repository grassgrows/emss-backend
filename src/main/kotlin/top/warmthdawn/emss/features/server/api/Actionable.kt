package top.warmthdawn.emss.features.server.api

/**
 * 可操作的
 * @author WarmthDawn
 * @since 2021-07-17
 */
interface Actionable {
    suspend fun start()
    suspend fun stop()
    suspend fun terminate()
    suspend fun restart()
}