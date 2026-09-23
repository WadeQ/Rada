import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

// Release signing reads from local.properties (git-ignored) so the real keystore path and
// passwords never touch source control or chat. Generate your own upload keystore with:
//   keytool -genkey -v -keystore rada-upload.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias rada
// then add these four lines to local.properties (values are yours, not shared here):
//   RADA_RELEASE_STORE_FILE=/absolute/path/to/rada-upload.keystore
//   RADA_RELEASE_STORE_PASSWORD=...
//   RADA_RELEASE_KEY_ALIAS=rada
//   RADA_RELEASE_KEY_PASSWORD=...
// Until they're set, `assembleRelease` still builds — just unsigned, exactly like `assembleDebug`
// does today. Environment variables of the same names work too, for CI later.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(FileInputStream(file))
}
fun releaseSigningProperty(key: String): String? = localProperties.getProperty(key) ?: System.getenv(key)
val releaseStoreFile = releaseSigningProperty("RADA_RELEASE_STORE_FILE")

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.rada.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rada.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = releaseSigningProperty("RADA_RELEASE_STORE_PASSWORD")
                keyAlias = releaseSigningProperty("RADA_RELEASE_KEY_ALIAS")
                keyPassword = releaseSigningProperty("RADA_RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // Minification is off for now: it needs a real signed build to test against R8's
            // effect on Firebase/Billing/Compose before it's safe to turn on. Do that once you
            // can sideload and run a release build, then flip this on and retest.
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (releaseStoreFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

configurations.all {
    resolutionStrategy {
        force("androidx.core:core:1.13.1")
        force("androidx.core:core-ktx:1.13.1")
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.car.app:app:1.7.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-functions")
    implementation("com.android.billingclient:billing-ktx:9.1.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    implementation("com.google.maps.android:maps-compose:6.12.2")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
