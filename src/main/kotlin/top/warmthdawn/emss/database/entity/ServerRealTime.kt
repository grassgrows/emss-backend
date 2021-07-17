package top.warmthdawn.emss.database.entity

import io.ebean.annotation.DbDefault
import top.warmthdawn.emss.features.server.ServerStatus
import top.warmthdawn.emss.features.server.entity.ServerState
import java.time.LocalDateTime
import javax.persistence.Entity

/**
 * @author takanashi
 * @date 2021/7/13
 */

@Entity
class ServerRealTime (
    var lastCrashDate: LocalDateTime? = null, //最后崩溃时间
    var lastStartDate: LocalDateTime? = null, //最后启动时间
    var state: ServerState,   //服务器状态
    var serverId: Long,
): BaseEntity()