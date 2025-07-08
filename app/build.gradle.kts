import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "io.github.kitswas.virtualgamepadmobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.kitswas.virtualgamepadmobile"
        // Defines the minimum API level required to run the app.
        minSdk = 26
        // Specifies the API level used to test the app.
        targetSdk = 34
        versionCode = 5
        versionName = "0.3.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
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
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.fromTarget("17")
        }
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

val props = Properties()
val propFile = File("signing.properties")

if (propFile.canRead()) {
    props.load(FileInputStream(propFile))

    if (props.containsKey("STORE_FILE") && props.containsKey("STORE_PASSWORD") &&
        props.containsKey("KEY_ALIAS") && props.containsKey("KEY_PASSWORD")
    ) {
        android.signingConfigs.getByName("release").apply {
            storeFile = File(props["STORE_FILE"].toString())
            println("Using keystore at: ${storeFile?.absolutePath}")
            storePassword = props["STORE_PASSWORD"].toString()
            keyAlias = props["KEY_ALIAS"].toString()
            keyPassword = props["KEY_PASSWORD"].toString()
        }
    } else {
        println("signing.properties found but some entries are missing")
        android.buildTypes.getByName("release").signingConfig = null
    }
} else {
    println("signing.properties not found")
    android.buildTypes.getByName("release").signingConfig = null
}

dependencies {
    // Compose BOM
    implementation(platform(libs.compose.bom))
    testImplementation(platform(libs.compose.bom))
    androidTestImplementation(platform(libs.compose.bom))

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
    testImplementation(libs.kotlinx.coroutines.test)
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

    // QR Code Scanner
    implementation(libs.zxing.android.embedded)
    implementation(libs.zxing.core)

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
