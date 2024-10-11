plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val versionMajor = 1
val versionMinor = 0
val versionPatch = 0
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
    compileSdk = 34

    defaultConfig {
        applicationId = "de.kirchewds.kirchenfuehrung.client"
        minSdk = 22
        targetSdk = 33
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // JDK support
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.0.4")
    // API
    implementation("io.gitlab.jfronny:gson:2.10.3-SNAPSHOT")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Images
    implementation("io.coil-kt:coil-compose:2.6.0")
    // Audio
    implementation("androidx.media3:media3-exoplayer:1.4.0")
    implementation("androidx.media3:media3-session:1.4.0")
    implementation("androidx.media3:media3-datasource-okhttp:1.4.0")
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.activity:activity-compose:1.9.1")
    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-android-compiler:2.52")
    // Other
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.9.24"))
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.8.1")
    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kapt {
    correctErrorTypes = true
}