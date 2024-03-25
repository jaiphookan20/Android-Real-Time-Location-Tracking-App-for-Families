plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") version "4.4.0"
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("dagger.hilt.android.plugin")
    kotlin("kapt")
    // id("com.ncorti.ktfmt.gradle") version "0.10.0"
    id("com.google.relay") version "0.3.09"
    id("kotlin-parcelize")
}

android {
    namespace = "org.comp90018.peopletrackerapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.comp90018.peopletrackerapp"
        minSdk = 28
        targetSdk = 33
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true // Enables viewbinding for XML Views
        compose = true // Enables Jetpack Compose
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }
}

dependencies {
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    //https://developer.android.com/jetpack/compose/setup
    val composeBom = platform("androidx.compose:compose-bom:2023.05.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Material Design 3
    implementation("androidx.compose.material3:material3")

    //Material Design 2
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")

    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // UI Tests
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Jetpack Compose integration with activities
    implementation("androidx.activity:activity-compose:1.6.1")

    // Jetpack Compose integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    // Default dependencies
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Firebase (Might not need all)
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")
    implementation("com.google.firebase:firebase-config-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-database-ktx")

    // Jetpack Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")
    implementation("com.google.accompanist:accompanist-permissions:0.30.1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.21")

    // Dagger dependencies
    implementation("com.google.dagger:hilt-android:2.47")
    kapt("com.google.dagger:hilt-android-compiler:2.47")

    // Google Maps
    implementation("com.google.maps.android:maps-compose:3.1.1")
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    // Jetpack Compose integration with livedata
    implementation("androidx.compose.runtime:runtime-livedata")

    //Retro fit for sending push notifications
    implementation("com.squareup.retrofit2:retrofit:2.6.2")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.retrofit2:converter-gson:2.6.0")

    // Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.maps.android:maps-compose:3.1.1")

    // Optionally, you can include the Compose utils library for Clustering, etc.
    implementation("com.google.maps.android:maps-compose-utils:3.1.1")

    // Optionally, you can include the widgets library for ScaleBar, etc.
    implementation("com.google.maps.android:maps-compose-widgets:3.1.1")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.4.0")

    implementation("com.github.yalantis:ucrop:2.2.6")
}

//kapt {
//    correctErrorTypes = true
//}