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
        versionCode = 8
        versionName = "0.3.4"

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

    // See https://gitlab.com/fdroid/fdroiddata/-/merge_requests/24636#note_2619840233
    // DependencyInfoBlock cannot be read by anyone other than Google.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
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
    // Implementation dependencies
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.window)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.material3.window.size)
    implementation(libs.navigation.compose)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.zxing.android.embedded)
    implementation(libs.zxing.core)
    implementation(platform(libs.compose.bom))
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Test dependencies
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(platform(libs.compose.bom))

    // AndroidTest dependencies
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.navigation.testing)
    androidTestImplementation(platform(libs.compose.bom))

    // Debug dependencies
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.tooling)
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
