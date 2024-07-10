plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.bugsnag.android)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zacharee1.insomnia"
        namespace = "com.zacharee1.insomnia"
        minSdk = 24
        targetSdk = 34
        versionCode = 10
        versionName = "1.8"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    implementation(libs.kotlin.stdlib.jdk8)

    implementation(libs.appcompat)
    implementation(libs.preference.ktx)
    implementation(libs.constraintlayout)

    implementation(libs.gson)
    implementation(libs.material)

    implementation(libs.hiddenapibypass)

    implementation(libs.bugsnag.android)
    implementation(libs.bugsnag.android.performance)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.activity.compose)
    implementation(libs.accompanist.themeadapter.material3)
}
