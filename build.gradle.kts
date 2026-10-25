import xyz.jpenilla.runpaper.task.RunServer

import java.net.URI

plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("com.gradleup.shadow") version "9.1.0"
}

group = "org.evlis"
version = "3.1.0"

val targetJavaVersion = 25
val junitVersion = "5.12.2"

repositories {
    mavenCentral()
    
    maven {
        name = "jitpack.io"
        url = URI("https://jitpack.io")
    }
    
    maven {
        name = "papermc-repo"
        url = URI("https://repo.papermc.io/repository/maven-public/")
    }
    
    maven {
        name = "sonatype"
        url = URI("https://oss.sonatype.org/content/groups/public/")
    }
    
    maven {
        name = "aikars-framework"
        url = URI("https://repo.aikar.co/content/groups/aikar/")
    }
}

dependencies {
    // Compile against the oldest supported Paper API.
    compileOnly("io.papermc.paper:paper-api:26.2.build.123-stable")
    
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    
    // MockBukkit tests target Paper 26.2.
    testImplementation(
        "org.mockbukkit.mockbukkit:mockbukkit-v26.2:4.116.1"
    )
    
    testImplementation(
        "io.papermc.paper:paper-api:26.2.build.123-stable"
    )
    
    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.toVersion(targetJavaVersion)
    targetCompatibility = JavaVersion.toVersion(targetJavaVersion)
    toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
    options.isFork = true
    options.release.set(targetJavaVersion)
}

tasks {
    processResources {
        val pluginVersion = project.version.toString()
        
        inputs.property("version", pluginVersion)
        
        filteringCharset = "UTF-8"
        
        filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
            expand("version" to pluginVersion)
        }
    }
    
    build {
        dependsOn(shadowJar)
    }
    
    shadowJar {
        relocate("co.aikar.commands", "Lunamatic.acf")
        relocate("co.aikar.locales", "Lunamatic.locales")
    }
    
    test {
        useJUnitPlatform()
        
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    
    // Default local server: Paper 26.2
    runServer {
        minecraftVersion("26.2")
    }
}

// Folia 26.2
runPaper.folia.registerTask {
    minecraftVersion("26.2")
}

// Automated Paper server test.
// Usage:
// ./gradlew runServerTest -PmcVersion=26.2
// ./gradlew runServerTest -PmcVersion=26.3
tasks.register<RunServer>("runServerTest") {
    dependsOn(tasks.shadowJar)
    
    val mcVersion =
    project.findProperty("mcVersion") as String? ?: "26.2"
    
    minecraftVersion(mcVersion)
    
    // Separate test environment for each Minecraft version
    runDirectory = file("run-test-$mcVersion")
    
    downloadPlugins {
        github(
            "Ifiht",
            "AutoStop",
            "v1.2.0",
            "AutoStop-1.2.0.jar"
        )
    }
    
    pluginJars.from(tasks.shadowJar)
}

// Interactive Paper 26.2 server
tasks.register<RunServer>("runServerInteractive_26-2") {
    dependsOn(tasks.shadowJar)
    
    minecraftVersion("26.2")
    pluginJars.from(tasks.shadowJar)
}

// Interactive Paper 26.3 server
tasks.register<RunServer>("runServerInteractive_26-3") {
    dependsOn(tasks.shadowJar)
    
    minecraftVersion("26.3")
    pluginJars.from(tasks.shadowJar)
}

// Folia 26.3
runPaper.folia.registerTask {
    minecraftVersion("26.3")
}

// Check the latest test server log
tasks.register("checkServerLogs") {
    doLast {
        val logFile = File("run/logs/latest.log")
        
        if (!logFile.exists()) {
            throw GradleException(
                "Log file not found: ${logFile.absolutePath}"
            )
        }
        
        val errorLines = logFile.readLines().filter {
            it.contains("ERROR]:")
        }
        
        if (errorLines.isNotEmpty()) {
            println("Errors were found:")
            errorLines.forEach(::println)
            throw GradleException("Errors found in log file.")
        } else {
            println("No errors found in log file.")
        }
    }
}