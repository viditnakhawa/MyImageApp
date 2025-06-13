plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("androidx.room")
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.coordinatorlayout)
    implementation(libs.material)
    implementation(libs.androidx.scenecore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // --- Jetpack Compose ---
    implementation(libs.ui)
    implementation(libs.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.activity.compose.v180)
    implementation(libs.androidx.lifecycle.runtime.ktx.v262)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.material3.window.size.class1)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)

    // --- Navigation ---
    implementation(libs.androidx.navigation.compose)

    // --- UI Helpers & Image Loading ---
    implementation(libs.coil.compose)
    implementation(libs.accompanist.permissions)

    // --- CameraX ---
    //val camerax_version = "1.4.2"
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // --- ML Kit & AI ---
    // The API we are currently using
    implementation(libs.genai.image.description)
    // Keep this for OCR, as it's used in a different file
    implementation(libs.text.recognition)
    //implementation(platform("com.google.mlkit:mlkit-bom:18.0.0"))

    // REMOVED: These libraries were conflicting with the genai-image-description/ML Kit API.
    //implementation("com.google.ai.edge.aicore:aicore:0.0.1-exp02")
    // implementation("com.google.mlkit:image-labeling:17.0.8")
    // implementation("com.google.mlkit:image-description:16.0.0")

    implementation(libs.tasks.genai)
    implementation(libs.accompanist.permissions)


    // --- Coroutines ---
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.coroutines.guava)

    // --- AndroidX (Legacy & Interop) ---
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // --- Tooling & Debugging ---
    implementation(libs.ui.tooling.preview)
    debugImplementation(libs.ui.tooling)

    implementation(libs.androidx.work.runtime.ktx)
    implementation("androidx.compose.runtime:runtime-livedata:1.6.0")
    implementation ("androidx.browser:browser:1.6.0")// or latest
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("net.openid:appauth:0.11.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.lifecycle:lifecycle-process:2.9.1")
    implementation("com.google.mediapipe:tasks-genai:0.10.24")
    implementation("org.tensorflow:tensorflow-lite:2.13.0")
    //implementation(libs.androidx.lifecycle.process)
    ksp("androidx.room:room-compiler:2.5.0")

    val room_version = "2.7.1"
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    implementation("androidx.compose.animation:animation:1.6.1")
    implementation("androidx.compose.ui:ui:1.6.1")

}
