import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

fun getProps(name: String): String {
    var propsFile = rootProject.file("local.properties")
    if(propsFile.exists() && propsFile.isFile) {
        var props = Properties()
        props.load(FileInputStream(propsFile))
        return props[name] as String
    } else {
        return ""
    }
}

android {
    namespace = "com.agrialert"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.agrialert"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "MAPBOX_API_KEY", getProps("MAPBOX_API_KEY"))
        }
        debug {
            buildConfigField("String", "MAPBOX_API_KEY", getProps("MAPBOX_API_KEY"))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-maps:19.2.0")
    implementation(libs.play.services.location)
    implementation(libs.work.runtime)
    implementation(libs.rules)
    val room_version = "2.8.4"
    implementation("androidx.room:room-runtime:${room_version}")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation("androidx.room:room-rxjava3:${room_version}")
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.mapbox.maps:android-ndk27:11.17.1")

}

