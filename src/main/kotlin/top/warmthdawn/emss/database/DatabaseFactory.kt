package top.warmthdawn.emss.database

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */
interface DatabaseFactory {
    fun connect()
    fun close()
}