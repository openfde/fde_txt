import java.text.SimpleDateFormat
import java.util.Date

plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.diffplug.spotless") version "5.11.0"
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.fde.txt"
    compileSdk = 33
    buildToolsVersion("33.0.1")

    defaultConfig {
        applicationId = "com.fde.txt"
        minSdk = 29
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 33
        versionCode = 100
        versionName = "100"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

   android {
        signingConfigs {
            create("release") {
                storeFile = file("../openfde.jks")
                storePassword = "keyforcat"
                keyAlias = "rootcloud"
                keyPassword = "keyforcat"
            }
        }
    
        buildTypes {
            getByName("release") {
                signingConfig = signingConfigs.getByName("release")
            }

            getByName("debug") {
                applicationIdSuffix = ".debug"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    lintOptions {
        disable("ExpiredTargetSdkVersion")
    }

    android.applicationVariants.all {
        val buildType = this.buildType.name
        val date = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        outputs.all {
            if (this is com.android.build.gradle
                .internal.api.ApkVariantOutputImpl
            ) {
                this.outputFileName = "FDE_txt.apk"
            }
        }
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.powerspinner)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}