import com.github.dockerjava.api.model.*
import top.warmthdawn.emss.features.docker.DockerManager

fun main() {
    val containerName = "testContainerMC"
    val bind = mutableListOf(Bind("E:\\EMSS\\$containerName\\minecraft_server.1.12.2", Volume("/data/")))
    val cmd = mutableListOf("/bin/sh", "-c", "java -jar /data/forge-1.12.2-14.23.5.2855.jar")
    val exposedPort = ExposedPort(80)
    val binding = Ports.Binding(null, 80.toString())
    val portBinding = mutableListOf(PortBinding(binding, exposedPort))
    val dockerId = DockerManager.createContainer(containerName, "openjdk:8", portBinding, bind, "/data/", cmd)

    if (dockerId == null) {
        print("*********** failed *************/n")
        return
    }

    DockerManager.startContainer(dockerId)

//        val buffer = ByteArray(1024 * 1024)
//        val input = ByteArrayInputStream(buffer)

    DockerManager.attachContainer(dockerId, System.`in`, System.out)

}