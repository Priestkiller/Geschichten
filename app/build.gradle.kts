plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val visualTestsEnabled = providers.gradleProperty("visualTests").orNull == "true"
val portableVisualTestsEnabled = providers.gradleProperty("portableVisualTests").orNull == "true"
val updateRepository = providers.gradleProperty("updateRepository").orElse("").get().trim()
require(updateRepository.isEmpty() || Regex("[A-Za-z0-9][A-Za-z0-9-]*/[A-Za-z0-9_.-]+").matches(updateRepository)) {
    "updateRepository must be a GitHub owner/repository pair."
}
val updateIncludePrerelease = providers.gradleProperty("updateIncludePrerelease").orElse("true").get().toBooleanStrict()

android {
    namespace = "dev.vincent.geschichten"
    compileSdk = 35
    ndkVersion = "27.2.12479018"

    defaultConfig {
        applicationId = "dev.vincent.geschichten"
        minSdk = 31
        targetSdk = 35
        versionCode = 22
        versionName = "0.8.8"
        buildConfigField("String", "UPDATE_REPOSITORY", "\"$updateRepository\"")
        buildConfigField("boolean", "UPDATE_INCLUDE_PRERELEASE", updateIncludePrerelease.toString())
        ndk { abiFilters += "arm64-v8a" }
        externalNativeBuild {
            cmake { arguments += "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON"; targets += "geschichten_gguf" }
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    signingConfigs {
        getByName("debug") {
            // Project-specific test key, generated for this prototype.
            // Preserve it for compatible private test updates; never use for release.
            storeFile = rootProject.file("signing/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources.excludes += setOf("/META-INF/AL2.0", "/META-INF/LGPL2.1")
    }

    if (visualTestsEnabled) {
        sourceSets.getByName("test").java.srcDir("src/visualTest/java")
        testOptions {
            unitTests.isIncludeAndroidResources = true
            unitTests.all { testTask ->
                if (portableVisualTestsEnabled) {
                    // Historical exporters depend on private probe files or an obsolete filler fixture.
                    testTask.exclude("**/ActiveMemoryModelFixturesTest*", "**/AnswerPipelineTest*",
                        "**/OwnershipAdverbPersistenceTest*", "**/RoleContaminationInvestigationTest*")
                }
                testTask.maxHeapSize = "2g"
                testTask.systemProperty("roborazzi.test.record", "true")
                testTask.systemProperty("robolectric.pixelCopyRenderMode", "hardware")
                testTask.systemProperty("robolectric.dependency.repo.url", "https://repo.maven.apache.org/maven2")
                // Robolectric resolves its Android test runtime in the test JVM.
                // Respect an existing Gradle proxy/trust-store configuration.
                listOf(
                    "http.proxyHost", "http.proxyPort", "http.proxyUser", "http.proxyPassword",
                    "https.proxyHost", "https.proxyPort", "https.proxyUser", "https.proxyPassword",
                    "javax.net.ssl.trustStore", "javax.net.ssl.trustStorePassword",
                ).forEach { key ->
                    System.getProperty(key)?.let { value -> testTask.systemProperty(key, value) }
                }
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.04.01")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("com.google.ai.edge.litertlm:litertlm-android:0.17.1")
    implementation("com.google.code.gson:gson:2.14.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    if (visualTestsEnabled) {
        testImplementation("org.robolectric:robolectric:4.16.1")
        testImplementation("androidx.test:core:1.6.1")
        testImplementation("androidx.test.ext:junit:1.2.1")
        testImplementation("androidx.compose.ui:ui-test-junit4")
        testImplementation("io.github.takahirom.roborazzi:roborazzi:1.50.0")
        testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.50.0")
        debugImplementation("androidx.compose.ui:ui-test-manifest")
    }
}
