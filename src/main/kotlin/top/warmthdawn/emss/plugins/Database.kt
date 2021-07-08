package top.warmthdawn.emss.plugins

import ch.qos.logback.classic.Logger
import io.ktor.application.*
import io.ktor.features.*
import org.koin.ktor.ext.getKoin
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.database.DatabaseFactory

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */
fun Application.configureDatabase() {
    val databaseFactory by inject<DatabaseFactory>()
    val db = databaseFactory.connect()
    getKoin().declare(db)
}