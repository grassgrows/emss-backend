package top.warmthdawn.emss.di

import org.koin.dsl.module
import org.koin.dsl.single
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.database.DBFactory
import top.warmthdawn.emss.database.DBFactoryImpl
import top.warmthdawn.emss.features.docker.ContainerService
import top.warmthdawn.emss.features.docker.ImageDownloadScheduler
import top.warmthdawn.emss.features.file.FileService
import top.warmthdawn.emss.features.server.ServerService
import top.warmthdawn.emss.features.settings.ImageService
import top.warmthdawn.emss.features.settings.SettingService

val appModule = module {
    // Backend Config
    single<AppConfig>()
    single<DBFactory> { DBFactoryImpl(get()) }

    //setting
    single { SettingService(get(), get()) }
    single { ImageDownloadScheduler(get()) }
    single { ImageService(get(), get(), get()) }

    //server
    single { ContainerService(get()) }
    single { ServerService(get(), get(), get(), get(), get()) }

    //file
    single { FileService() }
}
