val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val koin_version: String by project
val docker_java_version: String by project
//val ktorm_version: String by project
val ebean_version: String by project
val h2_version: String by project

plugins {
    application
    kotlin("jvm") version "1.5.20"
    id("io.ebean") version "12.9.1"
    kotlin("kapt") version "1.5.20-RC"
//    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.20"
}

group = "top.warmthdawn.emss"
version = "0.1.2"
application {
    mainClass.set("top.warmthdawn.emss.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
//    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-jackson:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
//    implementation("io.ktor:ktor-server-tomcat:$ktor_version")
//    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-cio:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-locations:$ktor_version")
//    implementation("com.fasterxml.jackson.module:jackson-modules-java8:2.12.4")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.4")
    // Koin
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    // Docker-Java
    implementation("com.github.docker-java:docker-java-core:$docker_java_version")
//    implementation("com.github.docker-java:docker-java-transport-httpclient5:$docker_java_version")
    implementation("com.github.docker-java:docker-java-transport-zerodep:$docker_java_version")
    // Ktorm
//    implementation("org.ktorm:ktorm-core:$ktorm_version")
    implementation("io.ebean:ebean:$ebean_version")
    kapt("io.ebean:kotlin-querybean-generator:$ebean_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("io.ebean:ebean-migration:12.4.0")
    // Testing
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("io.insert-koin:koin-test:$koin_version")
    testImplementation("junit:junit:4.12")
    testImplementation("io.ebean:ebean-test:$ebean_version")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
}