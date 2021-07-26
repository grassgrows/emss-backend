package top.warmthdawn.emss.database.entity

import top.warmthdawn.emss.features.server.entity.StatisticsType
import java.time.LocalDateTime
import javax.persistence.Entity

/**
 *
 * @author WarmthDawn
 * @since 2021-07-26
 */
@Entity
class ServerStatistics(
    var serverId: Long,
    var type: StatisticsType,
    var time: Long,
    var value: Double,
) : BaseEntity() {
    constructor(serverId: Long, type: StatisticsType, time: Long, value: Long) :
                this(serverId, type, time, value.toDouble())
    constructor(serverId: Long, type: StatisticsType, time: Long, value: Int) :
            this(serverId, type, time, value.toDouble())
}
