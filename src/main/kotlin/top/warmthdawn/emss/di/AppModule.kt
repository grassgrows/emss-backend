package top.warmthdawn.emss.di

import top.warmthdawn.emss.config.AppConfig
import org.koin.dsl.module
import org.koin.dsl.single

val appModule = module {
    // Backend Config
    single<AppConfig>()

}
