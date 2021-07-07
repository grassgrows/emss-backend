package top.warmthdawn.emss

import io.ktor.http.*
import io.ktor.server.testing.*
import top.warmthdawn.emss.utils.withTestServer
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() {
        withTestServer {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}