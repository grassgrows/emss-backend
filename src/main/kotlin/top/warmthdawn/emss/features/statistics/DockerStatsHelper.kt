package top.warmthdawn.emss.features.statistics

import com.github.dockerjava.api.model.BlkioStatsConfig
import com.github.dockerjava.api.model.StatisticNetworksConfig

/**
 *
 * @author WarmthDawn
 * @since 2021-07-27
 */
object DockerStatsHelper {
    fun calculateBlockIO(blkio: BlkioStatsConfig): Pair<Long, Long> {
        var blkRead: Long = 0
        var blkWrite: Long = 0
        for (bioEntry in blkio.ioServiceBytesRecursive!!) {
            if (bioEntry.op.isEmpty()) {
                continue
            }
            when (bioEntry.op[0]) {
                'r', 'R' ->
                    blkRead += bioEntry.value
                'w', 'W' ->
                    blkWrite += bioEntry.value
            }
        }
        return Pair(blkRead, blkWrite)
    }

    fun calculateNetwork(network : Map<String, StatisticNetworksConfig>) :Pair<Long, Long> {
        var rx = 0L
        var tx = 0L
        for((_,v) in network) {
            rx += v.rxBytes!!
            tx += v.txBytes!!
        }
        return Pair(rx, tx)
    }


}