package top.warmthdawn.emss.features.statistics.minecraft.ping

import top.limbang.doctor.client.MinecraftClient
import java.util.concurrent.TimeUnit

/**
 *
 * @author WarmthDawn
 * @since 2021-07-21
 */
object MinecraftTools{
    val timeout = 10L
    fun ping(port: Int) = kotlin.runCatching {
        MinecraftClient.ping("localhost", port, timeout,  TimeUnit.SECONDS)
    }.getOrNull()
}

