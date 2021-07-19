package top.warmthdawn.emss.features.server

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.server.impl.ServerObjectFactory
import top.warmthdawn.emss.utils.withTestServer
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 *
 * @author WarmthDawn
 * @since 2021-07-19
 */
class ServerNewTest {
    @Test
    fun startTest() {
        withTestServer(false) {
            val factory by application.inject<ServerObjectFactory>()
            factory.createAction(1).start()
            var curr = factory.create(1).currentState
            assertTrue {
                factory.create(1).running
            }
            delay(3000)
            factory.createAction(1).stop()
            curr = factory.create(1).currentState
            assertFalse {
                factory.create(1).running
            }
        }
    }
}