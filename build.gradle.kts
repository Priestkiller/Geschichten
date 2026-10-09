buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // LiteRT-LM 0.17.1 uses Kotlin 2.4. The Android compatibility table
        // requires D8/R8 9.1.29 or later for those class files.
        classpath("com.android.tools:r8:9.1.56")
    }
}

plugins {
    id("com.android.application") version "8.9.2" apply false
    id("org.jetbrains.kotlin.android") version "2.4.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.0" apply false
}
