import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

val localProperties = Properties().apply {
    val file = rootProject.file("mobile/local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

val rootEnvProperties = Properties().apply {
    val file = rootProject.file(".env")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

val webEnvProperties = Properties().apply {
    val file = rootProject.file("web/knockknock/.env")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun envValue(vararg names: String): String {
    for (name in names) {
        val value = localProperties.getProperty(name)
            ?: System.getenv(name)
            ?: rootEnvProperties.getProperty(name)
            ?: webEnvProperties.getProperty(name)

        if (!value.isNullOrBlank()) return value
    }

    return ""
}

fun androidString(value: String): String {
    return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}

android {
    namespace = "edu.villadarez.knockknock"
    compileSdk = 34

    defaultConfig {
        applicationId = "edu.villadarez.knockknock"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "SUPABASE_URL",
            androidString(envValue("VITE_SUPABASE_URL", "SUPABASE_URL"))
        )
        buildConfigField(
            "String",
            "SUPABASE_PUBLISHABLE_KEY",
            androidString(envValue("VITE_SUPABASE_PUBLISHABLE_DEFAULT_KEY", "SUPABASE_PUBLISHABLE_KEY", "SUPABASE_KEY"))
        )
        buildConfigField(
            "String",
            "SUPABASE_STORAGE_BUCKET",
            androidString(envValue("SUPABASE_STORAGE_BUCKET").ifBlank { "kk_files" })
        )
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
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    // Android Core & AppCompat
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)

    // UI Components (Using latest Material 3)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Retrofit & Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Image Loading (Glide)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Secure Storage for JWT
    implementation(libs.androidx.security.crypto)

    // Lifecycle (for Coroutines in Activities)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.core)
}
