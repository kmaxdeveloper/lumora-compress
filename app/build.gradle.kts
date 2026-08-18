plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    namespace = "uz.kmax.compress"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "uz.kmax.compress"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 2
        versionName = "1.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        buildConfigField("String", "BILLING_PUBLIC_KEY", "\"\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // The real public key will be injected here during final configuration
            buildConfigField("String", "BILLING_PUBLIC_KEY", "\"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmyEUvo3PEFb8NmLF71hkjmNE3IrM/F3gfTEqg4WfwxFTHsHIkBCkAAXy2UXdXCnnMWpiVrkuT4ftf1wridWdVdL9bD+Pq5HhCH3NV9I8GUXMHxYIn9awpAxTWYXWjrMGPhWFGbz1ivpuH7sD+v0m4/h29COw5H0A9TzI/CRbfVBoIYDUh4vrsDGBXgCMr+F6G3LZhOR3jkxPDxIaI6G/KNz5/4y5ZuPkcO+AsnqbdHsiAU9fKVbCthH1lZIW/1kwQgve5yFvd5Cp2x0L6nZpmsEWBnb5MWreqnzzl1A6Pv7KeiRVbgpNKhf7Hnhwl4gb7jR+9WUrAB2f0nnpFSOI5wIDAQAB\"")
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    configurations.all {
        resolutionStrategy {
            force("androidx.core:core:1.13.1")
            force("androidx.core:core-ktx:1.13.1")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE*"
            excludes += "META-INF/NOTICE*"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.kotlin_module"
        }
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    lint {
        checkReleaseBuilds = false
        // Workaround for Lint bug with Kotlin 2.0+ and high SDK targets
        disable += "NullSafeMutableLiveData"
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.documentfile)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.coil)
    implementation(libs.photoview)
    
    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    // Paging
    implementation(libs.androidx.paging.runtime)

    // WorkManager
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
    implementation("androidx.startup:startup-runtime:1.2.0")

    // AdMob & Monetization
    implementation(libs.play.services.ads)
    implementation(libs.play.billing.ktx)
    implementation(libs.user.messaging.platform)
    implementation(libs.play.review.ktx)
    implementation(libs.play.update.ktx)
    implementation(libs.play.services.oss.licenses)
    implementation(libs.gson)
    implementation(libs.yandex.mobileads)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.config)
    implementation(libs.firebase.crashlytics)

    // Base Library
    implementation("com.github.kmaxdeveloper:baselibrary:1.7.1")

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.hilt.compiler)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}
