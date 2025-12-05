plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.lr1ne"
version = "Alpha 1.1"

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }

    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
}

dependencies {
    // В Maven scope 'provided' соответствует 'compileOnly' в Gradle.
    // Это значит, что эти библиотеки нужны для компиляции, но они уже есть на сервере
    // и не должны попадать в итоговый jar (если не используется shading особым образом).

    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.17.0")
}

java {
    // Установка версии Java (аналог maven-compiler-plugin source/target)
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    // Настройка кодировки при компиляции (аналог project.build.sourceEncoding)
    compileJava {
        options.encoding = "UTF-8"
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"

        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}