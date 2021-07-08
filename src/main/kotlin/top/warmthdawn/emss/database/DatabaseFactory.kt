package top.warmthdawn.emss.database

import org.ktorm.database.Database

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */
interface DatabaseFactory {
    fun connect(): Database
    fun close()
}