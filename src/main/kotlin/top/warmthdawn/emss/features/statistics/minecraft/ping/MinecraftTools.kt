package top.warmthdawn.emss.features.statistics.minecraft.ping

import top.limbang.doctor.client.MinecraftClient
import top.limbang.doctor.client.running.TpsEntity
import top.limbang.doctor.client.running.tpsTools
import java.util.concurrent.TimeUnit

/**
 *
 * @author WarmthDawn
 * @since 2021-07-21
 */
object MinecraftTools {
    val timeout = 5 * 1000L
    fun ping(port: Int) = kotlin.runCatching {
        MinecraftClient.ping("localhost", port, timeout, TimeUnit.SECONDS)
    }.getOrNull()

    fun tps(client: MinecraftClient) = kotlin.runCatching {
        val tpsEntityList = client.tpsTools.getTps().get()
        var tpsEntity: TpsEntity? = null
        for (i in tpsEntityList.indices) {
            if (tpsEntityList[i].dim == "Overall") {
                tpsEntity = tpsEntityList[i]
            }
        }
        tpsEntity?.tps ?: 20.0
    }.getOrNull()
}

