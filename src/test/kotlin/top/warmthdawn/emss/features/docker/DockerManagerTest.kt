package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.Volume
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.junit.Test
import kotlin.coroutines.resume


internal class DockerManagerTest {
    @Test
    fun pullImageTest() {
        val dockerManager = DockerManager(
            /*registryUser = "takanashirikka1412", registryPass = "docg20092010ker",
            registryMail = "1303170174@qq.com",*/ registryUrl = "https://index.docker.io/v1/"
        )
        val imageName = "java"
        val callback = GlobalScope.launch {
            dockerManager.pullImage(imageName)
            print("downloading...")
        }
    }

    @Test
    fun createContainerTest() {
        val dockerManager = DockerManager(
            /*registryUser = "takanashirikka1412", registryPass = "docg20092010ker",
            registryMail = "1303170174@qq.com",*/ registryUrl = "https://index.docker.io/v1/"
        )
        val containerName = "testContainer005"
        val bind = Bind("/data/$containerName", Volume("/data"))
        val cmd = listOf("/bin/sh", "-c", "while true; do echo hello world; sleep 1; done")
        //if()
        print(
            dockerManager.createContainer(
                containerName, "ubuntu",
                "127.0.0.1"/*"172.28.89.242"*/, 8083, 8084, bind, cmd
            )
        )
    }

    @Test
    fun startContainerTest() {
        val dockerManager = DockerManager(
            /*registryUser = "takanashirikka1412", registryPass = "docg20092010ker",
            registryMail = "1303170174@qq.com",*/ registryUrl = "https://index.docker.io/v1/"
        )
        val containerName = "testContainer005"
        dockerManager.startContainer(containerName)
    }

    @Test
    fun stopContainerTest() {
        val dockerManager = DockerManager(
            /*registryUser = "takanashirikka1412", registryPass = "docg20092010ker",
            registryMail = "1303170174@qq.com",*/ registryUrl = "https://index.docker.io/v1/"
        )
        val containerName = "testContainer005"
        dockerManager.stopContainer(containerName)
    }

    @Test
    fun restartContainerTest() {
        val dockerManager = DockerManager(
            /*registryUser = "takanashirikka1412", registryPass = "docg20092010ker",
            registryMail = "1303170174@qq.com",*/ registryUrl = "https://index.docker.io/v1/"
        )
        val containerName = "testContainer005"
        dockerManager.restartContainer(containerName)
    }


    /*
@Test
fun buildImageTest()
{
    val dockerManager = DockerManager(registryUser = "takanashirikka1412",registryPass = "docg20092010ker",
        registryMail = "1303170174@qq.com",registryUrl = "https://index.docker.io/v1/")

    print(dockerManager.buildImage("DockerfileDir/Dockerfile", "testimage001"))
}
*/

}