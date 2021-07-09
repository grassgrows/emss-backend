package top.warmthdawn.emss.database

import io.ebean.Database

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */
interface DBFactory {
    fun connect(): Database
    fun close()
}