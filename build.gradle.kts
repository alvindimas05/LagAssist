import kotlin.properties.Delegates

plugins {
    id("java")
//    id("io.papermc.paperweight.userdev") version "1.7.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.minebench.de/")
    maven("https://repo.maven.apache.org/maven2/")
}


lateinit var minecraftVersion: String
var javaVersion by Delegates.notNull<Int>()
lateinit var versionName: String

task<Exec>("env") {
    minecraftVersion = System.getenv("MC_VERSION") ?: "1.21.1"
    javaVersion = 21
    versionName = minecraftVersion

    if (minecraftVersion == "legacy"){
        minecraftVersion = "1.20.3"
        javaVersion = 21
        versionName = "legacy"
    }
}

group = "org.alvindimas05.lagassist"
version = "2.32-$versionName"
description = "LagAssist"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}


dependencies {
//    paperweight.paperDevBundle("$minecraftVersion-R0.1-SNAPSHOT")

    compileOnly("io.papermc.paper:paper-api:$minecraftVersion-R0.1-SNAPSHOT")
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")

    implementation("com.google.code.gson:gson:2.11.0")
    implementation("io.netty:netty-all:4.1.111.Final")
    implementation("org.apache.directory.studio:org.apache.commons.lang:2.6")
    implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
    implementation("org.apache.clerezza.ext:org.json.simple:0.4")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

//tasks.assemble {
//    dependsOn(tasks.reobfJar)
//}
