plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "cl.vitacerca.demo"
    compileSdk = 35

    defaultConfig {
        applicationId = "cl.vitacerca.demo"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "0.1-demo"
    }
    buildTypes { getByName("release") { isMinifyEnabled = false } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
