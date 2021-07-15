package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.model.*
import io.ktor.util.*
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.file.FileService
import kotlin.io.path.Path

/**
 * @author takanashi
 * @since 2021-07-13
 */

/**
 * 由于单元测试无法测试流输入，故另新建文件，用main函数测试
 */

fun mmain() {
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
    DockerManager.attachContainer(dockerId, System.`in`, System.out)

}

fun main()
{
    val path = Path(QSetting().type.eq(SettingType.SERVER_ROOT_DIRECTORY).findOne()!!.value)
    print(path.toString()+"\n")
    val path2 = Path("/asdad")
    print(path2.toString()+"\n")
    val rootPath = path.combineSafe(path2)
    print(rootPath.toString()+"\n")

    val fileService = FileService()
    print(fileService.processPath("/root/qwert/").toString()+"\n")
    print(fileService.processPath("root/qwert/").toString()+"\n")
    print(fileService.processPath("/root/qwert").toString()+"\n")
    print(fileService.processPath("root/qwert").toString()+"\n")
//    print(fileService.processPath("\\root\\qwert\\").toString()+"\n")
//    print(fileService.processPath("root\\qwert\\").toString()+"\n")
//    print(fileService.processPath("\\root\\qwert").toString()+"\n")
//    print(fileService.processPath("root\\qwert").toString()+"\n")
}