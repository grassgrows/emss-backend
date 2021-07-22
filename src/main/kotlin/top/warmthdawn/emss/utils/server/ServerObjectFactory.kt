package top.warmthdawn.emss.utils.server

import io.ebean.Database
import top.warmthdawn.emss.features.command.CommandService
import top.warmthdawn.emss.features.docker.DockerService
import top.warmthdawn.emss.features.statistics.impl.StatisticsService
import top.warmthdawn.emss.features.system.NotificationService
import top.warmthdawn.emss.utils.server.api.ServerInstance
import top.warmthdawn.emss.utils.server.impl.ServerInstanceImpl

/**
 *
 * @author WarmthDawn
 * @since 2021-07-20
 */
class ServerInstanceFactory(
    private val db: Database,
    private val dockerService: DockerService,
    private val commandService: CommandService,
    private val statisticsService: StatisticsService,
    private val notificationService: NotificationService,
) {
    suspend fun create(id: Long): ServerInstance {
        return ServerInstanceImpl(
            id,
            db,
            dockerService,
            commandService,
            statisticsService,
            notificationService,
        ).apply {
            init()
        }
    }
}