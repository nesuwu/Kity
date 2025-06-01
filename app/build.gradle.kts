plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
}

android {
    namespace = "com.nesu.kity"
    compileSdk = 35 // You can use 34 or 35, ensure your AGP is compatible if using 35

    defaultConfig {
        applicationId = "com.nesu.kity"
        minSdk = 29 // Android Q
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true // If you prefer view binding, or remove if using findViewById
    }
}

dependencies {

    implementation(libs.androidx.core.ktx) // Or the version your project started with
    implementation(libs.androidx.appcompat) // Or the version your project started with
    implementation(libs.material) // Or the version your project started with
    implementation(libs.androidx.constraintlayout)
    implementation(libs.gson)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Activity Result Contracts (for file picker)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx) // activity-ktx often brings this, but good to be explicit

    // Network calls (Retrofit and OkHttp)
    implementation(libs.retrofit)
    implementation(libs.converter.scalars) // For plain string responses from Catbox
    implementation(libs.okhttp) // Retrofit brings a version of this, but explicit is fine
    implementation(libs.logging.interceptor) // Helpful for debugging network calls

    // Image Loading (Coil)
    implementation(libs.coil)
    implementation(libs.androidx.viewpager2) // Add if not present
    implementation(libs.androidx.fragment.ktx) // Add if not present or ensure it's there

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}