package top.warmthdawn.emss.database

import io.ebean.DB
import io.ebean.Database
import io.ebean.DatabaseFactory
import io.ebean.config.DatabaseConfig
import io.ebean.datasource.DataSourceConfig

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */
class DBFactoryImpl : DBFactory {
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
        return DatabaseFactory.create(config)
    }

    override fun close() {
    }
}