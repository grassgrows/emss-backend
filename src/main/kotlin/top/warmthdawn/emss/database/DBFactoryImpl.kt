package top.warmthdawn.emss.database

import io.ebean.DB
import io.ebean.Database
import io.ebean.DatabaseFactory
import io.ebean.config.DatabaseConfig
import io.ebean.datasource.DataSourceConfig
import top.warmthdawn.emss.config.AppConfig

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */
class DBFactoryImpl(
    private val appConfig: AppConfig,
) : DBFactory {
    override fun connect(): Database {
//        return Database.connect(
//            url = "jdbc:mysql://localhost:3306/ktorm",
//            driver = "com.mysql.jdbc.Driver",
//            user = "root",
//            password = "***"
//        )

        val config = DatabaseConfig().apply {
            loadFromProperties()
            isDefaultServer = true
        }
        if(appConfig.testing && appConfig.useMemory) {
            config.dataSourceConfig.url = "jdbc:h2:mem:test;DATABASE_TO_UPPER=false;MODE=MYSQL"
        }

        return DatabaseFactory.create(config)
    }

    override fun close() {
    }
}