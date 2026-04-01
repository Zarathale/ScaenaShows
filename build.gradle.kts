plugins {
    java
    id("com.gradleup.shadow") version "8.3.6"
}

group = "com.scaena"
version = "2.21.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    // SnakeYAML is bundled in the Paper server jar at runtime; compileOnly here for compilation.
    compileOnly("org.yaml:snakeyaml:2.2")
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    processResources {
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(mapOf("version" to project.version))
        }
    }

    shadowJar {
        archiveClassifier.set("")  // Output: ScaenaShows-2.21.0.jar (no "-all" suffix)
    }

    build {
        dependsOn(shadowJar)
    }
}
