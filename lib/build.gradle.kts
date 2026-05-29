@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.maven.publish)
}

group = "io.github.vyfor"
version = "0.1.0"

repositories {
    mavenCentral()
}

@Suppress("OPT_IN_USAGE")
kotlin {
    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()
    iosArm64()
    iosSimulatorArm64()
    iosX64()
    js {
        browser()
        nodejs()
    }
    jvm()
    linuxArm64()
    linuxX64()
    macosArm64()
    macosX64()
    mingwX64()
    tvosArm64()
    tvosSimulatorArm64()
    tvosX64()
    wasmJs {
        browser()
        nodejs()
    }
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    watchosX64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.cio)
        }
    }
}

tasks.register("compileAllTargets") {
    group = "build"
    dependsOn(
        kotlin.targets.mapNotNull { target ->
            val name = "compileKotlin${target.name.replaceFirstChar { it.titlecase() }}"
            tasks.findByName(name)
        },
    )
}

mavenPublishing {
    configure(KotlinMultiplatform(javadocJar = JavadocJar.Empty(), sourcesJar = true))

    coordinates("io.github.vyfor", "klipy-kt", project.version.toString())

    pom {
        name.set("klipy-kt")
        description.set("Async Kotlin Multiplatform client for the KLIPY API")
        url.set("https://github.com/vyfor/klipy-kt")
        inceptionYear.set("2026")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("repo")
            }
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("vyfor")
                name.set("vyfor")
                url.set("https://github.com/vyfor/")
            }
        }
        scm {
            url.set("https://github.com/vyfor/klipy-kt/")
            connection.set("scm:git:git://github.com/vyfor/klipy-kt.git")
            developerConnection.set("scm:git:ssh://git@github.com/vyfor/klipy-kt.git")
        }
    }

    publishToMavenCentral()
    signAllPublications()
}
