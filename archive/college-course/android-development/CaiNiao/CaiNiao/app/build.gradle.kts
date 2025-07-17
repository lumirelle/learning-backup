plugins {
  alias(libs.plugins.androidApplication)
}

android {
  namespace = "org.seagulls.cainiao"
  compileSdk = 34

  defaultConfig {
    applicationId = "org.seagulls.cainiao"
    minSdk = 24
    targetSdk = 34
    versionCode = 5
    versionName = "1.0.0-rc.4"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    multiDexEnabled = true
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
  buildFeatures {
    viewBinding = true
  }
}

dependencies {
  compileOnly(libs.lombok)
  annotationProcessor(libs.lombok)

  implementation(libs.glide)
  implementation(libs.core)
  implementation(libs.zxing.android.embedded)
  implementation(libs.multidex)
  implementation(libs.fastjson2)
  implementation(libs.okhttp)
  implementation(libs.appcompat)
  implementation(libs.material)
  implementation(libs.constraintlayout)
  implementation(libs.vectordrawable)
  implementation(libs.lifecycle.livedata.ktx)
  implementation(libs.lifecycle.viewmodel.ktx)
  implementation(libs.navigation.fragment)
  implementation(libs.navigation.ui)

  testImplementation(libs.junit)
  androidTestImplementation(libs.ext.junit)
  androidTestImplementation(libs.espresso.core)
}
