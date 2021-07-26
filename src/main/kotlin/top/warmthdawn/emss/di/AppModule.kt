package top.warmthdawn.emss.di

import org.koin.dsl.module
import org.koin.dsl.single
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.database.DBFactory
import top.warmthdawn.emss.database.DBFactoryImpl
import top.warmthdawn.emss.features.command.CommandService
import top.warmthdawn.emss.features.docker.DockerService
import top.warmthdawn.emss.features.docker.ImageDownloadScheduler
import top.warmthdawn.emss.features.file.FileService
import top.warmthdawn.emss.features.permission.PermissionService
import top.warmthdawn.emss.features.login.LoginService
import top.warmthdawn.emss.features.server.ServerService
import top.warmthdawn.emss.features.statistics.StatisticsService
import top.warmthdawn.emss.features.settings.ImageService
import top.warmthdawn.emss.features.settings.SettingService
import top.warmthdawn.emss.features.system.NotificationService
import top.warmthdawn.emss.features.system.SystemService

val appModule = module {
    // Backend Config
    single<AppConfig>()
    single<DBFactory> { DBFactoryImpl(get()) }

    //setting
    single { SettingService(get(), get(), get(), get()) }
    single { ImageDownloadScheduler(get()) }
    single { ImageService(get(), get(), get()) }

    //server
    single { ServerService(get(), get(), get(), get()) }
    //docker
    single { DockerService(get(), get()) }
    //status
    single { StatisticsService(get(), get()) }
    //file
    single { FileService(get()) }
    //command
    single { CommandService(get()) }
    //system
    single { SystemService() }
    single { NotificationService() }

    //permission
    single { PermissionService(get(), get()) }
    single { LoginService(get(), get()) }
}
