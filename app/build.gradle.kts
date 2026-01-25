import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

fun getProps(name: String): String {
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists() && propsFile.isFile) {
        val props = Properties()
        props.load(FileInputStream(propsFile))
        val value = props.getProperty(name) ?: ""
        return "\"$value\""
    } else {
        return "\"\""
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

tasks.register<Javadoc>("generateAndroidJavadoc") {
    group = "documentation"
    description = "Genera Javadoc per il codice Java ignorando gli errori di moduli esterni"

    dependsOn("compileReleaseJavaWithJavac", "dataBindingGenBaseClassesRelease")

    // 1. Indica dove sono i tuoi file sorgente (.java)
    setSource(files(
        android.sourceSets.getByName("main").java.srcDirs,
        layout.buildDirectory.dir("generated/data_binding_base_class_source_out/release/out")
    ))

    // 2. Configura il Classpath (serve per fargli trovare le classi Android come Activity, Context, ecc.)
    doFirst {
        val androidBootClasspath = files(android.bootClasspath)
        // Prende le dipendenze dalla variante di release
        val dependencyClasspath = android.applicationVariants.find { it.name == "release" }
            ?.javaCompileProvider?.get()?.classpath?.files ?: files()

        classpath = androidBootClasspath + files(dependencyClasspath) + files(layout.buildDirectory.dir("generated/source/buildConfig/release"))
    }

    // 3. Opzioni fondamentali per evitare l'errore dei moduli
    options {
        this as StandardJavadocDocletOptions

        // Disabilita i controlli severi (questo risolve l'errore module-info)
        addStringOption("Xdoclint:none", "-quiet")

        // Imposta la codifica corretta
        encoding = "UTF-8"
        charSet = "UTF-8"

        // Includi anche i membri privati e protetti? (opzionale)
        memberLevel = JavadocMemberLevel.PRIVATE

        // Titolo della pagina
        windowTitle = "AgriAlert Documentation"
    }

    // 4. Escludi i file generati automaticamente da Android (R.java e BuildConfig)
    exclude("**/R.java", "**/BuildConfig.java")


}
