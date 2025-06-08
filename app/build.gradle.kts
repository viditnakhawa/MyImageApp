plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation ("androidx.compose.ui:ui:1.6.1")
    implementation ("androidx.compose.material3:material3:1.2.0")
    implementation ("androidx.activity:activity-compose:1.8.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation ("io.coil-kt:coil-compose:2.5.0") // For image loading
    implementation ("androidx.navigation:navigation-compose:2.7.4")
    implementation ("androidx.compose.foundation:foundation:1.6.1")
    implementation ("androidx.compose.material3:material3-window-size-class:1.1.2")
    val camerax_version = "1.4.2"
    implementation ("androidx.camera:camera-core:${camerax_version}")
    implementation ("androidx.camera:camera-camera2:${camerax_version}")
    implementation ("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation ("androidx.camera:camera-view:${camerax_version}")

    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation ("androidx.appcompat:appcompat:1.6.1") // Or the latest version
    // Other dependencies like material design, constraint layout etc.
    //implementation "com.google.android.material:material:1.11.0" // Or latest
    //implementation "androidx.constraintlayout:constraintlayout:2.1.4" // Or latest
    implementation ("com.google.android.material:material:1.12.0")

    implementation ("com.google.mlkit:text-recognition:16.0.1")
    implementation ("com.google.mlkit:image-labeling:17.0.8")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")

    implementation ("androidx.compose.material:material-icons-core:1.6.8")
    implementation ("androidx.compose.material:material-icons-extended:1.6.8")

    implementation("com.google.ai.edge.aicore:aicore:0.0.1-exp01")
    implementation("com.google.mlkit:genai-image-description:1.0.0-beta1")

}