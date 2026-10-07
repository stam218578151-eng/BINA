import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// Release signing — keystore.properties (gitignored) holds the store/key
// passwords; it's absent on CI/other machines, so release signing there
// silently falls back to unsigned rather than failing the build.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "iam699030.gmail.movitop"
    compileSdk = 33

    defaultConfig {
        applicationId = "iam699030.gmail.movitop"
        minSdk = 19
        targetSdk = 19
        multiDexEnabled = true
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Vector drawables are used throughout the UI. Enable the AndroidX backport for API 19.
        vectorDrawables {
            useSupportLibrary = true
        }

        // API 19 devices are generally 32-bit. The Java/UI layer can install at
        // minSdk 19, but the offline MOTIS engine additionally needs a real
        // armeabi-v7a native binary for 32-bit KitKat devices.
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        buildConfig = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    // The MOTIS engine binary ships as app/src/main/jniLibs/<abi>/libmotis.so
    // (see build_motis_arm64.sh). It must land on disk under the app's
    // nativeLibraryDir as a real, installer-extracted file — Android 10+
    // forbids exec() of anything the app itself wrote (and can therefore
    // also make executable) inside its own writable data directory, so a
    // copy-to-filesDir-then-chmod approach is blocked on real devices.
    // useLegacyPackaging forces that on-disk extraction instead of leaving
    // native libs page-aligned/mmapped straight out of the (compressed) APK.
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation("androidx.multidex:multidex:2.0.1")
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)

    // Mapsforge offline vector maps
    implementation(libs.mapsforge.core)
    implementation(libs.mapsforge.map)
    implementation(libs.mapsforge.map.reader)
    implementation(libs.mapsforge.themes)
    implementation(libs.mapsforge.map.android)
    implementation(libs.androidsvg)

    // Networking (MOTIS REST API)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}