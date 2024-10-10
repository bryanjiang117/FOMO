plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
  namespace = "com.example.fomo"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.example.fomo"
    minSdk = 24
    targetSdk = 34
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions { jvmTarget = "1.8" }
  buildFeatures {
    compose = true
    buildConfig = true
  }
}

secrets {
  // properties file containing secrets
  propertiesFileName = "secrets.properties"
  // properties file containing default secret values (when actual secret doesn't exist)
  defaultPropertiesFileName = "local.defaults.properties"
  // Configure which keys should be ignored by the plugin by providing regular expressions.
  // "sdk.dir" is ignored by default.
  ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
  ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.voyager.navigator)

  // Google Maps
  implementation(libs.places)
  implementation(libs.play.services.maps)
  implementation(libs.google.maps.compose)
  implementation(libs.google.maps.compose.utils)
  implementation(libs.google.maps.compose.widgets)

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}
