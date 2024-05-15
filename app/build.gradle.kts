plugins {
    id("com.android.application")
}

android {
    namespace = "com.prelude.preludeplayer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.prelude.preludeplayer"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

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
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.android.exoplayer:exoplayer:2.14.1")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("androidx.activity:activity:1.8.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation ("com.github.TutorialsAndroid:FilePicker:v8.0.19")

    //Equalizer
    implementation ("com.github.bullheadandplato:AndroidEqualizer:2.2")
}