// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // To benefit from the latest Performance Monitoring plugin features,
    // update your Android Gradle plugin dependency to at least v3.4.0
    //id("com.android.application") version "7.3.0" apply false

    // Make sure that you have the Google services Gradle plugin dependency
    id("com.google.gms.google-services") version "4.4.4" apply false

    // Add the dependency for the Performance Monitoring Gradle plugin
    id("com.google.firebase.firebase-perf") version "2.0.2" apply false

    // Add the dependency for the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics") version "3.0.6" apply false
}