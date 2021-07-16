package top.warmthdawn.emss.database

import io.ebean.annotation.Platform
import io.ebean.dbmigration.DbMigration

/**
 *
 * @author WarmthDawn
 * @since 2021-07-09
 */
fun main() {
    val dbMigration = DbMigration.create()
    dbMigration.setPlatform(Platform.H2)
    dbMigration.generateMigration()
    dbMigration.generateInitMigration()
}