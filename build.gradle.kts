plugins {
    id("java")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.minebench.de/")
    maven("https://repo.maven.apache.org/maven2/")
}

group = "org.alvindimas05.lagassist"
version = "2.30.1"
description = "LagAssist"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("net.milkbowl.vault:VaultAPI:1.7")

    implementation("io.netty:netty-all:4.1.111.Final")
    implementation("org.apache.directory.studio:org.apache.commons.lang:2.6")
    implementation("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
    implementation("org.apache.clerezza.ext:org.json.simple:0.4")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}