package top.warmthdawn.emss.database.entity

import top.warmthdawn.emss.features.server.ServerStatus
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
    var status: ServerStatus   //服务器状态
): BaseEntity()