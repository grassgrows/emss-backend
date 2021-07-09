package top.warmthdawn.emss.di

import top.warmthdawn.emss.config.AppConfig
import org.koin.dsl.module
import org.koin.dsl.single
import top.warmthdawn.emss.database.DBFactory
import top.warmthdawn.emss.database.DBFactoryImpl
import top.warmthdawn.emss.features.settings.SettingService

val appModule = module {
    // Backend Config
    single<AppConfig>()
    single<DBFactory> { DBFactoryImpl() }

    //setting
    single { SettingService(get()) }
}
