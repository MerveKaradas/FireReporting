plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    alias(libs.plugins.google.gms.google.services)
    //id("com.google.gms.google-services")//fireb için ekledim

}

android {
    namespace = "com.mervekaradas.yanginvardemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mervekaradas.yanginvardemo"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) //fireb için ekledim
    implementation("com.google.firebase:firebase-analytics") //fireb için ekledim
    implementation("com.google.firebase:firebase-auth") //fireb için ekledim


    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    implementation("com.google.android.gms:play-services-location:21.3.0")


    /* implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")// Retrofit2'de parametreleri raw olarak göndermek istediğimizde Skaler kullanmamoz gerekli

*/














}