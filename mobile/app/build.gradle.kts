plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "edu.cit.villadarez.knockknock"
    compileSdk = 34

    defaultConfig {
        applicationId = "edu.cit.villadarez.knockknock"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Android Core & AppCompat
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)


    // These two work together with the one above
    implementation("androidx.appcompat:appcompat:1.6.1")
    // UI Components (Using latest Material 3)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Retrofit & Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0") // 3.0.0 is still in alpha/beta, 2.11.0 is safer
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Secure Storage for JWT
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Lifecycle (for Coroutines in Activities)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
