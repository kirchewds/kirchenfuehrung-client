plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dagger.hilt)
}

val versionMajor = 1
val versionMinor = 0
val versionPatch = 1
val versionBuild = System.getenv("CI_PIPELINE_IID")?.toIntOrNull() ?: 0

val computedVersionName by lazy { "$versionMajor.$versionMinor.$versionPatch" + if (versionBuild > 0) "+$versionBuild" else "" }

// Version code: S VVVVV MMMMMMM PPPPP IIIIIIIIIIIIII (32-bit integer)
// S (x1):  Sign bit. Must always be 0 for an android version code
// V (x5):  Major version. Up to 32, which should be enough (especially since we are still on 0)
//          This might not work for apps that follow proper semantic versioning, but who does that?
// M (x7):  Minor version. Up to 128, which should be enough
// P (x5):  Patch version. Up to 32, which should be enough for these
// I (x14): Pipeline ID bits. Allows a total of 16384 pipeline runs.
//          I'm simply guessing that that'll be enough
//
// This implementation assumes that these maximum numbers will never be reached.
// If they are reached, the version codes "bleed over" into the next range,
// so this should technically still produce valid, higher versions, but the format will be broken.
val computedVersionCode by lazy {
    var bits = 0
    bits = (bits shl 5) or versionMajor
    bits = (bits shl 7) or versionMinor
    bits = (bits shl 5) or versionPatch
    bits = (bits shl 14) or versionBuild
    bits
}

android {
    namespace = "de.kirchewds.kirchenfuehrung.client"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.kirchewds.kirchenfuehrung.client"
        minSdk = 22
        targetSdk = 35
        versionCode = computedVersionCode
        versionName = computedVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // JDK support
    coreLibraryDesugaring(libs.jdk.desugar)
    // API
    implementation(libs.gson)
    implementation(libs.okhttp)
    // Images
    implementation(libs.coil.compose)
    // Audio
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.datasource.okhttp)
    implementation(libs.media3.exoplayer.workmanager)
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.material.icons.extended)
    implementation(libs.material3.window.size)
    implementation(libs.navigation.compose)
    implementation(libs.activity.compose)
    // Hilt
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)
    // Other
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(platform(libs.kotlin.bom))
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.guava)
    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}

kapt {
    correctErrorTypes = true
}
