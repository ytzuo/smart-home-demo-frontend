plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.SmartHome.SmartHomeDemo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.SmartHome.SmartHomeDemo"
        minSdk = 24
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
        }
        getByName("debug") {
            isJniDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    sourceSets["main"].jniLibs.srcDirs("libs/jni")
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(files("libs\\ZRDDSd.jar"))
    implementation(libs.room.common.jvm)
    implementation(libs.room.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    annotationProcessor(libs.room.compiler)
//    implementation("cn.pedant.sweetalert:library:1.3") //提示框的依赖, 可以用来提示用户敏感信息或弹出Alert
//    implementation("jp.wasabeef:recyclerview-animators:4.0.2") //recycle view动效库
}