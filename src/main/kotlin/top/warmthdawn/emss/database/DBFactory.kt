package top.warmthdawn.emss.database

import io.ebean.Database

interface DBFactory {
    fun connect(): Database
    fun close()
}
