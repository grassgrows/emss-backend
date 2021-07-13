package top.warmthdawn.emss.features.docker;

import com.github.dockerjava.api.model.*
import org.junit.Ignore
import org.junit.Test;
import top.warmthdawn.emss.features.docker.vo.ContainerStatus
import java.io.ByteArrayInputStream






@Ignore
internal class MCTest {
    @Test
    fun windowsCreateContainerTest() {
        val containerName = "testContainer010"
        val bind = mutableListOf(Bind("E:\\EMSS\\$containerName", Volume("/")))
        val cmd = mutableListOf("/bin/sh", "-c", "cat /test.txt")
        val exposedPort = ExposedPort(8084)
        val binding = Ports.Binding(null, 8083.toString())
        val portBinding = mutableListOf(PortBinding(binding, exposedPort))
        DockerManager.createContainer(containerName, "openjdk:8", portBinding, bind, "/data", cmd)
    }

    @Test
    fun linuxCreateContainerTest() {
        val containerName = "testContainer010"
        val bind = mutableListOf(Bind("/home/takanashiunix/$containerName", Volume("/data/")))
        val cmd = mutableListOf("/bin/sh", "-c", "cat /data/test.txt")
        val exposedPort = ExposedPort(8084)
        val binding = Ports.Binding(null, 8083.toString())
        val portBinding = mutableListOf(PortBinding(binding, exposedPort))
        DockerManager.createContainer(containerName, "openjdk:8", portBinding, bind, "/data", cmd)
    }

    @Test
    fun inputTest()
    {
        val c = readLine()
        print(c)
    }

}
