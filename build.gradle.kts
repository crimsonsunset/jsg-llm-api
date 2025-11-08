plugins {
    kotlin("jvm") version "2.1.21"
    application
}

group = "dev.mokksy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.mokksy.aimocks:ai-mocks-openai-jvm:0.6.1")
    implementation("io.github.serpro69:kotlin-faker:1.16.0")
    // SLF4J simple implementation to silence "No SLF4J providers" warning
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

application {
    mainClass.set("MockServerKt")
    // Suppress Java warnings for native access and deprecated methods
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED",
        "-Dio.netty.tryReflectionSetAccessible=true"
    )
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "MockServerKt"
        )
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Custom tasks for convenience
tasks.register("dev") {
    group = "application"
    description = "Run in development mode (kills existing processes on port 8080 first)"
    doFirst {
        println("Killing processes on port 8080...")
        // Kill any process using port 8080
        exec {
            commandLine("sh", "-c", "lsof -ti:8080 | xargs kill -9 2>/dev/null || true")
            isIgnoreExitValue = true
        }
        // Also kill any gradle run processes
        exec {
            commandLine("pkill", "-f", "gradle.*run")
            isIgnoreExitValue = true
        }
        // Wait for processes to fully terminate
        Thread.sleep(2000)
        println("Port 8080 cleared, starting server...")
    }
    dependsOn("run")
}

// Suppress Gradle native access warnings
tasks.withType<JavaExec>().configureEach {
    jvmArgs = listOf("--enable-native-access=ALL-UNNAMED")
}

tasks.register("docker-build") {
    group = "docker"
    description = "Build Docker image"
    doLast {
        exec {
            commandLine("docker", "build", "-t", "jsg-llm-api:latest", ".")
        }
    }
}

tasks.register("docker-run") {
    group = "docker"
    description = "Run Docker container"
    dependsOn("docker-build")
    doLast {
        exec {
            commandLine("docker", "stop", "jsg-llm-api")
            isIgnoreExitValue = true
        }
        exec {
            commandLine("docker", "rm", "jsg-llm-api")
            isIgnoreExitValue = true
        }
        exec {
            commandLine("docker", "run", "-d", "--name", "jsg-llm-api", "--network", "host", 
                        "-e", "PORT=8080", "-e", "VERBOSE=true", "jsg-llm-api:latest")
        }
    }
}

tasks.register("docker-stop") {
    group = "docker"
    description = "Stop Docker container"
    doLast {
        exec {
            commandLine("docker", "stop", "jsg-llm-api")
            isIgnoreExitValue = true
        }
        exec {
            commandLine("docker", "rm", "jsg-llm-api")
            isIgnoreExitValue = true
        }
    }
}

tasks.register("docker-logs") {
    group = "docker"
    description = "View Docker container logs"
    doLast {
        exec {
            commandLine("docker", "logs", "-f", "jsg-llm-api")
        }
    }
}

