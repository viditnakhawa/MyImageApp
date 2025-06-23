plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("androidx.room")
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    namespace = "com.viditnakhawa.myimageapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.viditnakhawa.myimageapp"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["appAuthRedirectScheme"] = "com.viditnakhawa.myimageapp.oauth"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    // --- Core & UI ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation(libs.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.accompanist.permissions)
    implementation("androidx.compose.runtime:runtime-livedata:1.6.8")
    implementation("androidx.compose.animation:animation:1.6.8")
    implementation("androidx.palette:palette-ktx:1.0.0")


    // --- CameraX ---
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // --- AI: On-Device ---
    // This is the main library for on-device Gemma inference via MediaPipe
    implementation(libs.mediapipe.tasks.vision)
    implementation(libs.mediapipe.tasks.genai)
    //implementation("com.google.mediapipe:framework:0.10.18")
    implementation("com.google.protobuf:protobuf-javalite:3.24.0")
    // These are for the fallback analysis when Gemma is not available
    implementation(libs.genai.image.description)
    implementation(libs.text.recognition)

    // --- Coroutines ---
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.guava)

    // --- App Architecture & Data ---
    implementation(libs.androidx.work.runtime.ktx)
    implementation("androidx.browser:browser:1.8.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("net.openid:appauth:0.11.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.lifecycle:lifecycle-process:2.8.3")
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.appcompat) // For interop

    // --- Room Database (Corrected) ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // --- Testing ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("com.google.android.gms:play-services-oss-licenses:17.1.0")

}