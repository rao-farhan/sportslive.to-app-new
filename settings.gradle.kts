import java.util.Properties

fun resolveProjectName(defaultName: String): String {
    val localPropertiesFile = file("local.properties")
    if (!localPropertiesFile.exists()) {
        return defaultName
    }

    val properties = Properties().apply {
        localPropertiesFile.inputStream().use { load(it) }
    }
    return properties.getProperty("PROJECT_NAME")?.takeIf { it.isNotBlank() } ?: defaultName
}

rootProject.name = resolveProjectName("ExampleApp")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":shared")
include(":desktopApp")
include(":androidApp")
include(":webApp")
