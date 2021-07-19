package top.warmthdawn.emss.features.server.impl

import io.ebean.Database
import kotlinx.coroutines.coroutineScope
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.features.command.CommandService
import top.warmthdawn.emss.features.docker.ContainerStatus
import top.warmthdawn.emss.features.docker.DockerService
import top.warmthdawn.emss.features.server.api.Actionable
import top.warmthdawn.emss.features.server.api.ServerObject
import top.warmthdawn.emss.features.server.entity.ServerState

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

    suspend fun init() {
        QServer(db).findIds<Long>().forEach {
            val test = dockerService.inspectContainer(it)
            when (test) {
                ContainerStatus.Unknown -> {
                    ServerPersistImpl(db, it).saveState(ServerState.INITIALIZE)
                }
                ContainerStatus.Running -> {
                    ServerPersistImpl(db, it).saveState(ServerState.RUNNING)
                }
                ContainerStatus.Stopped -> {
                    ServerPersistImpl(db, it).saveState(ServerState.STOPPED)
                }
            }
        }

    }

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