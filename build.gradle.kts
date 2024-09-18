// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0") // Match your current AGP version
        classpath("com.google.gms:google-services:4.3.15") // Google Services
        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3")
    }
}



