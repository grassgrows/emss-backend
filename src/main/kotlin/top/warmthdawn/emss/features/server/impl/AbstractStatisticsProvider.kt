package top.warmthdawn.emss.features.server.impl

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import top.warmthdawn.emss.features.server.api.StatisticsProvider
import top.warmthdawn.emss.features.server.entity.StatisticsInfo
import kotlin.coroutines.CoroutineContext

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
class AbstractStatisticsProvider(
    parentContext: CoroutineContext,
    abbr: String,
) : StatisticsProvider, CoroutineScope {
    private val job = Job()
    override fun getCurrent(): StatisticsInfo {
        TODO("Not yet implemented")
    }

    override fun getHistory(): List<StatisticsInfo> {
        TODO("Not yet implemented")
    }

    override val coroutineContext: CoroutineContext =
        CoroutineName("STATUS_PROVIDER($abbr)")
            .plus(parentContext)
            .plus(job)



}
