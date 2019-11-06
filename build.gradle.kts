object Version {
    const val KOTLIN = "1.3.21"
    const val KTOR = "1.1.3"
    const val JVM = "1.8"
    const val COROUTINES = "1.1.1"
    const val JUNIT = "5.3.2"
    const val JACKSON = "2.9.8"
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.21" apply true
    id("java-library") apply true
    id("maven") apply true
    id("idea") apply true
}

group = "io.klira."
version = "0.1.0"
description = "Wrapper library for requesting a machine-to-machine token from Auth0"

defaultTasks = mutableListOf("test")

repositories {
    mavenLocal()
    jcenter()
    mavenCentral()
    maven(url = "https://dl.bintray.com/kotlin/ktor")
    maven(url = "https://jitpack.io")
}

dependencies {
    compile("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", Version.KOTLIN)
    compile("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", Version.COROUTINES)
    
    // Logging
    implementation("io.github.microutils", "kotlin-logging", "1.6.20")

    // Jackson
    implementation("com.fasterxml.jackson.core", "jackson-core", Version.JACKSON)
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", Version.JACKSON)
    implementation("com.fasterxml.jackson.datatype", "jackson-datatype-jsr310", Version.JACKSON)

    api("com.squareup.okhttp3:okhttp:3.14.2")
    implementation("com.auth0", "java-jwt", "3.7.0")

    // Junit
    testCompile("org.junit.jupiter", "junit-jupiter-api", Version.JUNIT)
    testRuntime("org.junit.jupiter", "junit-jupiter-engine", Version.JUNIT)
}

tasks {
    test {
        useJUnitPlatform()

        // Show test results.
        testLogging {
            events("passed", "skipped", "failed")
        }
        reports {
            junitXml.isEnabled = false
            html.isEnabled = true
        }
    }

    compileKotlin {
        sourceCompatibility = Version.JVM
        kotlinOptions {
            jvmTarget = Version.JVM
        }
    }

    compileTestKotlin {
        sourceCompatibility = Version.JVM
        kotlinOptions {
            jvmTarget = Version.JVM
        }
    }


    wrapper {
        description = "Generates gradlew[.bat] scripts for faster execution"
        gradleVersion = "5.4.1"
    }
}
