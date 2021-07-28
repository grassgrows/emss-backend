package top.warmthdawn.emss.database.entity

import io.ebean.annotation.DbDefault
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
    var serverMaxPlayer: Int = 0,   //服务器最大玩家
    var serverPlayerNumber: Int = 0,   //服务器当前玩家
    var serverTps: Double = 20.0,   //服务器Tps
    var serverId: Long,
    @DbDefault("false")
    var autoRestart: Boolean = false,
): BaseEntity()