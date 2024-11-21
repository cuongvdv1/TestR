plugins {
    id("com.android.library")
    //alias(libs.plugins.jetbrains.kotlin.android)
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.lib"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false
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
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.github.ybq:Android-SpinKit:1.4.0")
    implementation("com.google.android.gms:play-services-ads:23.3.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation(project(":B9Util"))
    implementation ("androidx.lifecycle:lifecycle-process:2.8.5")

}