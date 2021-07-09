package top.warmthdawn.emss.plugins

import io.ktor.application.*
import org.koin.ktor.ext.getKoin
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.DBFactory
import top.warmthdawn.emss.database.default.tryInitDefault

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */
fun Application.configureDatabase() {
    val databaseFactory by inject<DBFactory>()
    val db = databaseFactory.connect()
    getKoin().declare(db)

    tryInitDefault()
}