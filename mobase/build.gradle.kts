plugins {
    alias(libs.plugins.android.library)
}

allprojects {
    group = "dev.mobase"
    version = "0.0.1"
}

android {
    namespace = "dev.mobase"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField(
            "String",
            "VERSION",
            "\"0.0.1\""
        )

        buildConfigField(
            "String",
            "APPSFLYER_SDK_VERSION",
            "\"${libs.appsflyer.get().version}\""
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    api(project(":purchases:api"))
    api(project(":common"))

    // Analytics
    implementation(libs.amplitude)
    implementation(libs.appsflyer)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // Feature flags
    implementation(libs.amplitude.experiment)

    // Session replay
    implementation(libs.amplitude.session.replay)

    // Attribution
    implementation(libs.android.installreferrer)
    implementation(libs.android.ads.identifier)
    implementation(libs.android.appset)

    // App update
    implementation(libs.android.app.update)

    // Storage
    implementation(libs.androidx.datastore)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.play.services)

    // UI (deeplink)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Logging
    implementation(libs.timber)
    implementation(libs.androidx.lifecycle.process)
}
