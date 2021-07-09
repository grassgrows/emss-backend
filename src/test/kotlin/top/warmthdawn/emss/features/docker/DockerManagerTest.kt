package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.Volume
import org.junit.Test


internal class DockerManagerTest {
    @Test
    fun pullImageTest() {
        val imageName = "java"
        print("downloading...")
        val callback = DockerManager.pullImage(imageName)
        callback.awaitCompletion()

    }

    @Test
    fun createContainerTest() {
        val containerName = "testContainer006"
        val bind = Bind("/data/$containerName", Volume("/data"))
        val cmd = listOf("/bin/sh", "-c", "while true; do echo hello world; sleep 1; done")
        //if()
        print(
            DockerManager.createContainer(
                containerName, "ubuntu",
                "127.0.0.1"/*"172.28.89.242"*/, 8083, 8084, bind, cmd
            )
        )
    }

    @Test
    fun startContainerTest() {
        val containerName = "testContainer005"
        DockerManager.startContainer(containerName)
    }

    @Test
    fun stopContainerTest() {
        val containerName = "testContainer005"
        DockerManager.stopContainer(containerName)
    }

    @Test
    fun inspectContainerTest() {
        val containerName = "0" // testContainer006 0818c1f30f607aefe2a722431445af50baf526600576f5a5f3e2169511662d27
        val container = DockerManager.inspectContainer(containerName)

        if (container != null) {
            print("******** "+container.id+" ********\n")
            print("******** "+container.name+" ********\n")
            print("******** "+container.imageId+" ********\n")
            print("******** "+container.status+" ********\n")
        }
        else
        {
            print("******** not found ********\n")
        }

    }

}