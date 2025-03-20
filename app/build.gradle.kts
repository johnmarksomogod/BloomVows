plugins {
    id("com.android.application")
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services") // Firebase integration
}

android {
    namespace = "com.example.plannerwedding"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.plannerwedding"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

    // **Fix for Duplicate META-INF Issue**
    packagingOptions {
        exclude ("META-INF/NOTICE.md")
        exclude ("META-INF/LICENSE.md")
    }
}

dependencies {
    // Firebase BOM (ensures consistent versions for Firebase dependencies)
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))


    // Firebase Dependencies
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    // Color Picker Library
    implementation("com.github.skydoves:colorpickerview:2.2.4")

    // Email Support
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    // AndroidX Core & UI Components
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Navigation Components
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // Glide (for image loading)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // Material Calendar View (For Dot Indicators on Calendar Dates)
    implementation("com.prolificinteractive:material-calendarview:1.4.3")

    // RecyclerView for smoother scrolling
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Media3 Library (if required, otherwise remove)
    implementation("androidx.media3:media3-common:1.2.0")
    implementation(libs.androidx.datastore.core.android)
    implementation(libs.firebase.functions.ktx)
}
