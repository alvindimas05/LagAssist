
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


group = "org.alvindimas05.lagassist"
version = "2.32.2"
description = "LagAssist"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}


dependencies {
//    paperweight.paperDevBundle("$minecraftVersion-R0.1-SNAPSHOT")

    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")

    implementation("com.google.code.gson:gson:2.11.0")
    implementation("io.netty:netty-all:4.2.0.RC1")
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
