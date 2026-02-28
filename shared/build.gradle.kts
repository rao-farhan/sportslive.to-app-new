import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.buildkonfig)
}

// Access the reusable function from the root project
val getRequiredProperty: (String) -> String by rootProject.extra

val baseUrl = getRequiredProperty("BASE_URL")
val apiKey = getRequiredProperty("API_KEY")
val xorKey = getRequiredProperty("XOR_KEY").toInt()
val appVersionName = getRequiredProperty("APP_VERSION_NAME")
val appVersionCode = getRequiredProperty("APP_VERSION_CODE")
val appId = getRequiredProperty("APP_ID")

fun encode(value: String, key: Int): String {
    return value.map { (it.code xor key).toChar() }.joinToString("")
}

val encodedApiKey = encode(apiKey, xorKey)

buildkonfig {
    packageName = "to.sports.live"
    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "BASE_URL", baseUrl)
        buildConfigField(FieldSpec.Type.STRING, "ENCODED_API_KEY", encodedApiKey)
        buildConfigField(FieldSpec.Type.INT, "XOR_KEY", xorKey.toString())
        buildConfigField(FieldSpec.Type.STRING, "APP_ID", appId)
        buildConfigField(FieldSpec.Type.STRING, "APP_VERSION_NAME", appVersionName)
        buildConfigField(FieldSpec.Type.STRING, "APP_VERSION_CODE", appVersionCode)
    }
}

kotlin {
    androidLibrary {
        namespace = "to.sports.live.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            // Serialization
            implementation(libs.kotlinx.serialization.json)
            // Datetime
            implementation(libs.kotlinx.datetime)
            // Ktor Core & Plugins
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            // SQLDelight
            implementation(libs.sqldelight.runtime)
            // Multiplatform Settings
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.no.arg)
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            // Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // Napier
            implementation(libs.napier)
            // Decompose
            implementation(libs.decompose)
            implementation(libs.decompose.extensions.compose)
            // MVIKotlin
            implementation(libs.mvikotlin)
            implementation(libs.mvikotlin.main)
            implementation(libs.mvikotlin.extensions.coroutines)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.koin.android)
            implementation(libs.store5)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
            implementation(libs.store5)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
            implementation(libs.store5)
        }

        jsMain.dependencies {
            implementation(libs.ktor.client.js)
            implementation(libs.store5)
        }

        @OptIn(ExperimentalWasmDsl::class)
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
                // Store5 currently doesn't support wasmJs
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("to.sports.live.db")
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
