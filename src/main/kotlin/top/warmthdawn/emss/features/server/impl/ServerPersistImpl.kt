package top.warmthdawn.emss.features.server.impl

import io.ebean.Database
import top.warmthdawn.emss.database.entity.ServerRealTime
import top.warmthdawn.emss.database.entity.query.QServerRealTime
import top.warmthdawn.emss.features.server.api.ServerPersist
import top.warmthdawn.emss.features.server.entity.ServerState
import java.time.LocalDateTime

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
class ServerPersistImpl(
    private val db: Database,
    private val id: Long
) : ServerPersist {


    override fun getRunning(): ServerRealTime {
        return QServerRealTime(db).id.eq(id).findOne()!!
    }

    override fun updateRunning(lastCrashDate: LocalDateTime?, lastStartDate: LocalDateTime?) {
        QServerRealTime(db).id.eq(id).findOne()?.let {
            if (lastCrashDate != null) {
                it.lastCrashDate = lastCrashDate
            }
            if (lastStartDate != null) {
                it.lastStartDate = lastStartDate
            }
            it.update()
        }
    }

    override fun saveState(state: ServerState) {
        QServerRealTime(db).id.eq(id).findOne()?.let {
            it.state = state
            it.update()
        }
    }

    override fun getState(): ServerState {
        return QServerRealTime(db).id.eq(id).select(QServerRealTime._alias.state).findOne()!!.state
    }

}