package top.warmthdawn.emss.features.server.impl

import io.ebean.Database
import kotlinx.coroutines.coroutineScope
import top.warmthdawn.emss.features.command.CommandService
import top.warmthdawn.emss.features.docker.DockerService
import top.warmthdawn.emss.features.server.api.Actionable
import top.warmthdawn.emss.features.server.api.ServerObject

/**
 *
 * @author WarmthDawn
 * @since 2021-07-19
 */
class ServerObjectFactory(
    private val db: Database,
    private val dockerService: DockerService,
    private val commandService: CommandService,
    private val statisticsService: StatisticsService,
) {

    suspend fun create(id: Long): ServerObject = coroutineScope {
        ServerObjectImpl(
            db,
            id,
            dockerService,
            commandService,
            statisticsService,
            coroutineContext,
        )
    }

    suspend fun createAction(id: Long): Actionable {
        return ServerActionImpl(create(id), dockerService)
    }

}