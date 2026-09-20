plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

val isMacHost = System.getProperty("os.name").contains("Mac", ignoreCase = true)

kotlin {
    jvmToolchain(17)

    androidTarget()

    if (isMacHost) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { target ->
            target.binaries.framework {
                baseName = "Shared"
                isStatic = true
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("com.google.android.gms:play-services-location:21.3.0")
                implementation("androidx.core:core-ktx:1.13.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
            }
        }
    }
}

android {
    namespace = "com.rada.shared"
    compileSdk = 36
    buildToolsVersion = "35.0.0"
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}