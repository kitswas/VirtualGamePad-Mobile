plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "io.github.kitswas.virtualgamepadmobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.kitswas.virtualgamepadmobile"
        // Defines the minimum API level required to run the app.
        minSdk = 26
        // Specifies the API level used to test the app.
        targetSdk = 34
        versionCode = 4
        versionName = "0.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    dependenciesInfo {
        includeInApk = true
        includeInBundle = true
    }
}

dependencies {
    // Image Loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.svg)

    // Core Libraries
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.window)

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose UI
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Navigation
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    // Feature module Support
    implementation(libs.navigation.dynamic.features.fragment)
    // Testing Navigation
    androidTestImplementation(libs.navigation.testing)
    // Jetpack Compose Integration
    implementation(libs.navigation.compose)

    // Integrate the Play Feature Delivery Library
    implementation(libs.play.feature.delivery)
    implementation(libs.play.feature.delivery.ktx)

    // Google code scanner API for QR code scanning
    implementation(libs.play.services.code.scanner)
    implementation(libs.play.services.base)
    implementation(libs.play.services.tflite.java)

    // Material3
    implementation(libs.material3)
    implementation(libs.material3.window.size)

    // Datastore
    implementation(libs.androidx.datastore.preferences)
}

val updateVGPDataExchangePackage by tasks.registering(ProcessResources::class) {
    println("Updating VGP_Data_Exchange package...")
    from(rootDir.toPath().resolve("VGP_Data_Exchange/io/github/kitswas/VGP_Data_Exchange"))
    into(rootDir.toPath().resolve("app/src/main/java/io/github/kitswas/VGP_Data_Exchange"))
}

tasks.matching {
    it != updateVGPDataExchangePackage.get()
}.configureEach {
    dependsOn(updateVGPDataExchangePackage)
}
