package top.warmthdawn.emss.database

import org.ktorm.database.Database

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */
class DatabaseFactoryImplSqlLite : DatabaseFactory {
    override fun connect(): Database {
        return Database.connect(
            url = "jdbc:mysql://localhost:3306/ktorm",
            driver = "com.mysql.jdbc.Driver",
            user = "root",
            password = "***"
        )

    }

    override fun close() {
    }
}