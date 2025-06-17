plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias (libs.plugins.safeargs)
    alias(libs.plugins.ksp)

    //dagger hilt
    alias(libs.plugins.hilt.android)
    kotlin("kapt")

}

android {
    namespace = "com.example.shinobimusic"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.shinobimusic"
        minSdk = 24
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

    buildFeatures {
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // Media3 (ExoPlayer + MediaSession)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)
    implementation(libs.media3.ui)
    implementation(libs.kotlinx.coroutines.guava)

    //nav component
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    //room
    implementation(libs.room)
    implementation(libs.room.runtime)
    implementation(libs.androidx.palette.ktx)
    annotationProcessor(libs.room.compiler)
    ksp(libs.room.compiler)

    // Dagger Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Optional - if you're using Navigation Compose
    implementation(libs.hilt.navigation.compose)



    // Coroutines
    implementation(libs.kotlinx.coroutines.android)    // Android dispatchers

    // ViewModel & Lifecycle
    implementation(libs.lifecycle.viewmodel.ktx)       // ViewModel + Coroutines
    implementation(libs.lifecycle.runtime.ktx)         // LifecycleScope

    //glide
    implementation (libs.glide)
    implementation(libs.gson)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}