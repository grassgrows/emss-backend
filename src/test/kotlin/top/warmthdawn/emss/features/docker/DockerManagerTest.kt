package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.model.*
import io.ebean.config.JsonConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Ignore
import org.junit.Test
import org.koin.java.KoinJavaComponent.inject
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.utils.withTestServer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * @author takanashi
 * @since 2021-07-07
 */

/**
 * Docker需要客户端参与不方便进行单元测试。Ignore掉
 */

@Ignore
internal class DockerManagerTest {
    @Test
    fun pullImageTest() {
        val imageName = "java"
        print("downloading...")
        //val callback = DockerManager.pullImage(imageName)
        //callback.awaitCompletion()
    }

    @Test
    fun createContainerTest() {
        val containerName = "testContainer008"
        val bind = mutableListOf(Bind("/data/$containerName", Volume("/data")))
        val cmd = mutableListOf("/bin/sh", "-c", "while true; do echo hello world; sleep 1; done")
        val exposedPort = ExposedPort(8084)
        val binding = Ports.Binding(null, 8083.toString())
        val portBinding = mutableListOf(PortBinding(binding, exposedPort))
        //if()
        print(
            DockerManager.createContainer(
                containerName, "ubuntu", portBinding, bind,"/data/", cmd
            )
        )
    }

    @Test
    fun getTimeTest() {
        withTestServer {
            val service by application.inject<ContainerService>()
            service.getContainerCreateTime("testContainer007")
        }
    }

    @Test
    fun startContainerTest() {
        val containerName = "testContainer007"
        DockerManager.startContainer(containerName)
    }

    @Test
    fun stopContainerTest() {
        val containerName = "testContainer007"
        DockerManager.stopContainer(containerName)
    }

    @Test
    fun inspectImageTest() {
        val imageId = "2"//"c49355e8fcc5c030257a9ae819c6179dd242eac7f98792f88368dc49fe23c47e"
        val image = DockerManager.inspectImage(imageId)

        if (image != null) {
            print("******** " + image.id + " ********\n")
            print("******** " + image.created + " ********\n")
            print("******** " + image.size + " ********\n")
        } else {
            print("******** not found ********\n")
        }
    }

    @Test
    fun inspectContainerTest() {
        val containerId =
            "testContainer006" // testContainer006 0818c1f30f607aefe2a722431445af50baf526600576f5a5f3e2169511662d27
        val container = DockerManager.inspectContainer(containerId)

        if (container != null) {
            // 自定义格式化:
            val myDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'")
            // 用自定义格式解析:
            val dt2 = LocalDateTime.parse(container.createTime, myDateTimeFormatter)
            print("******** ${dt2.year}年${dt2.monthValue}月${dt2.dayOfMonth}日${dt2.hour}时${dt2.minute}分${dt2.second}秒 ********\n")
            print("******** " + container.id + " ********\n")
            print("******** " + container.name + " ********\n")
            print("******** " + container.createTime + " ********\n")
            print("******** " + container.imageId + " ********\n")
            print("******** " + container.status + " ********\n")
        } else {
            print("******** not found ********\n")
        }

    }

    @Test
    fun removeImageTest() {
        val imageId = "2"//"c49355e8fcc5c030257a9ae819c6179dd242eac7f98792f88368dc49fe23c47e"

        if (DockerManager.removeImage(imageId))
            print("******** succeed ********\n")
        else
            print("******** fail ********\n")
    }

    @Test
    fun removeContainerTest() {
        val containerId = ""//"83d364defa655d5c149eff90ed9fa6cbeb04c8423e5505b9c87f1f939e592687"

        if (DockerManager.removeContainer(containerId))
            print("******** succeed ********\n")
        else
            print("******** fail ********\n")
    }


    @Test
    fun attachContainerTest() {
        DockerManager.attachContainer("testContainer007", System.`in`, System.out)
    }

}