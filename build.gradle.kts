// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://repo.huaweicloud.com/repository/maven") }
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
        google()
        // noinspection JcenterRepositoryObsolete
         jcenter()
        // google()
        // mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.20")
        classpath("com.github.ben-manes:gradle-versions-plugin:0.36.0")
    }
}

plugins {
alias(libs.plugins.jetbrains.kotlin.android) apply false

    //    alias(libs.plugins.android.application) apply false
//    alias(libs.plugins.jetbrains.kotlin.android) apply false
}