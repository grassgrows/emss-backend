package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.Volume
import org.junit.Test


internal class DockerManagerTest {
    @Test
    fun buildImageTest()
    {
        val dockerManager = DockerManager(registryUser = "takanashirikka1412",registryPass = "docg20092010ker",
            registryMail = "1303170174@qq.com",registryUrl = "https://index.docker.io/v1/")

        print(dockerManager.buildImage("DockerfileDir/Dockerfile", "testimage001"))
    }



    @Test
    fun createContainerTest() {
        val dockerManager = DockerManager(registryUser = "takanashirikka1412",registryPass = "docg20092010ker",
            registryMail = "1303170174@qq.com",registryUrl = "https://index.docker.io/v1/")
        val containerName = "testContainer004"
        val bind =Bind("/data/testContainer004", Volume("/data"))
        val cmd = listOf("/bin/sh", "-c", "while true; do echo hello world; sleep 1; done")
        //if()
        print(dockerManager.createContainer(containerName,"ubuntu",
        "127.0.0.1"/*"172.28.89.242"*/,8083,8084,bind, cmd))
    }

    @Test
    fun startContainerTest()
    {
        val dockerManager = DockerManager(registryUser = "takanashirikka1412",registryPass = "docg20092010ker",
            registryMail = "1303170174@qq.com",registryUrl = "https://index.docker.io/v1/")
        val containerName = "testContainer004"
        dockerManager.startContainer(containerName)
    }

    @Test
    fun stopContainerTest()
    {
        val dockerManager = DockerManager(registryUser = "takanashirikka1412",registryPass = "docg20092010ker",
            registryMail = "1303170174@qq.com",registryUrl = "https://index.docker.io/v1/")
        val containerName = "testContainer004"
        dockerManager.stopContainer(containerName)
    }

    @Test
    fun restartContainerTest()
    {
        val dockerManager = DockerManager(registryUser = "takanashirikka1412",registryPass = "docg20092010ker",
            registryMail = "1303170174@qq.com",registryUrl = "https://index.docker.io/v1/")
        val containerName = "testContainer004"
        dockerManager.restartContainer(containerName)
    }
}