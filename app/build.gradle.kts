plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.aplikasisholat" // Ganti sesuai package kamu
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.aplikasisholat"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // AndroidX core & UI
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Material Design (support M3 and M2 components)
    implementation("com.google.android.material:material:1.11.0")

    // Lifecycle dan Activity KTX
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.annotation:annotation:1.7.1")

    // Optional: RecyclerView untuk menampilkan daftar jadwal
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Optional: Jika ingin pakai lokasi dari Play Services (lebih akurat)
    // implementation("com.google.android.gms:play-services-location:21.0.1")

    // Optional: Jika ingin ganti SQLite ke Room
    // implementation("androidx.room:room-runtime:2.6.1")
    // kapt("androidx.room:room-compiler:2.6.1")
}
