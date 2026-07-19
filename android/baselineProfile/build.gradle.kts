plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.kotlin.android)
}

// Read android/local.properties (project root, already gitignored — never committed)
// via Gradle's own Provider API + plain Kotlin string parsing, instead of
// java.util.Properties — AGP 9's newDsl mode doesn't reliably resolve arbitrary JDK
// classes in this script, but providers.fileContents() is part of Gradle's own typed
// DSL surface, so it isn't affected. Expects these two lines in android/local.properties:
//   benchmark.test.email=your-test-account@example.com
//   benchmark.test.password=your-test-account-password
val localPropertiesText: String = providers.fileContents(
    rootProject.layout.projectDirectory.file("local.properties")
).asText.getOrElse("")

fun readLocalProperty(key: String): String =
    localPropertiesText.lineSequence()
        .map { it.trim() }
        .firstOrNull { it.startsWith("$key=") }
        ?.substringAfter("=")
        ?.trim()
        .orEmpty()

val benchmarkTestEmail: String = readLocalProperty("benchmark.test.email")
val benchmarkTestPassword: String = readLocalProperty("benchmark.test.password")

android {
    namespace = "com.albbiz.map.baselineprofile"
    compileSdk {
        version = release(37)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    defaultConfig {
        minSdk = 28
        targetSdk = 37

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Dedicated benchmark/QA account credentials — read from android/local.properties,
        // see the comment near the top of this file.
        testInstrumentationRunnerArguments["benchmarkTestEmail"] = benchmarkTestEmail
        testInstrumentationRunnerArguments["benchmarkTestPassword"] = benchmarkTestPassword
    }

    targetProjectPath = ":app"
    kotlinOptions {
        jvmTarget = "11"
    }

}

// This is the configuration block for the Baseline Profile plugin.
// You can specify to run the generators on a managed devices or connected devices.
baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.core.ktx)
}

androidComponents {
    onVariants { v ->
        val artifactsLoader = v.artifacts.getBuiltArtifactsLoader()
        v.instrumentationRunnerArguments.put(
            "targetAppId",
            v.testedApks.map { artifactsLoader.load(it)?.applicationId }
        )
    }
}