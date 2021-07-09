package top.warmthdawn.emss.features.docker

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
        val containerName = "testContainer001"
        //if()
        print(dockerManager.createContainer(containerName,"ubuntu",
            "172.28.89.242",8083,8084))

    }
}