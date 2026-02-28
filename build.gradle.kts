import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.sqldelight) apply false
}

// Reusable function to get required properties from local.properties
private fun Project.loadLocalProperties(): Properties {
    val localPropertiesFile = rootProject.file("local.properties")
    if (!localPropertiesFile.exists()) {
        throw GradleException("local.properties file is required for build. Please create it based on local.properties.example")
    }

    return Properties().apply {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun Project.getRequiredProperty(key: String): String {
    val properties = loadLocalProperties()
    return properties.getProperty(key)?.takeIf { it.isNotBlank() }
        ?: throw GradleException("Property '$key' is required in local.properties")
}

fun Project.getPropertyOrDefault(key: String, defaultValue: String): String {
    val localPropertiesFile = rootProject.file("local.properties")
    if (!localPropertiesFile.exists()) {
        return defaultValue
    }

    val properties = Properties().apply {
        localPropertiesFile.inputStream().use { load(it) }
    }
    return properties.getProperty(key)?.takeIf { it.isNotBlank() } ?: defaultValue
}

fun String.toPbxprojQuotedValue(): String {
    val escaped = this
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
    return "\"$escaped\""
}

fun upsertIosBuildSetting(buildSettings: String, key: String, value: String): String {
    val keyRegex = Regex("""(?m)^(\s*)$key = .*;$""")
    if (keyRegex.containsMatchIn(buildSettings)) {
        return buildSettings.replace(keyRegex) { matchResult ->
            "${matchResult.groupValues[1]}$key = $value;"
        }
    }

    val anchorRegex = Regex("""(?m)^(\s*)TARGETED_DEVICE_FAMILY = .*;$""")
    val anchorMatch = anchorRegex.find(buildSettings)
        ?: throw GradleException("Unable to insert iOS build setting '$key' because TARGETED_DEVICE_FAMILY was not found.")

    val indent = anchorMatch.groupValues[1]
    val insertedLine = "$indent$key = $value;\n"
    return buildSettings.replaceRange(anchorMatch.range.first, anchorMatch.range.last + 1, insertedLine + anchorMatch.value)
}

// Store the function in extra properties so subprojects can access it
extra.set("getRequiredProperty", { key: String -> getRequiredProperty(key) })
extra.set("getPropertyOrDefault", { key: String, defaultValue: String -> getPropertyOrDefault(key, defaultValue) })

tasks.register("syncIosConfig") {
    val iosProjectFile = rootProject.file("iosApp/iosApp.xcodeproj/project.pbxproj")

    doLast {
        val projectName = getPropertyOrDefault("PROJECT_NAME", rootProject.name)
        val appName = getRequiredProperty("APP_NAME")
        val appId = getRequiredProperty("APP_ID")
        val appVersionName = getRequiredProperty("APP_VERSION_NAME")
        val appVersionCode = getRequiredProperty("APP_VERSION_CODE")
        val teamId = getRequiredProperty("TEAM_ID")
        val appProductFileName = "$projectName.app"
        val quotedProjectName = projectName.toPbxprojQuotedValue()
        val quotedAppName = appName.toPbxprojQuotedValue()

        if (!iosProjectFile.exists()) {
            throw GradleException("iOS project file not found at ${iosProjectFile.path}")
        }

        val targetBuildSettingsBlockRegex = Regex(
            pattern = """(?s)(buildSettings = \{\n)(.*?INFOPLIST_FILE = iosApp/Info\.plist;.*?TARGETED_DEVICE_FAMILY = "1,2";\n)(\s*\};)"""
        )

        val pbxprojWithoutXcconfigReference = iosProjectFile.readText().replace(
            Regex("""(?m)^[ \t]*baseConfigurationReferenceAnchor = .*;\n^[ \t]*baseConfigurationReferenceRelativePath = Config\.xcconfig;\n"""),
            ""
        )

        val targetBlockCount = targetBuildSettingsBlockRegex.findAll(pbxprojWithoutXcconfigReference).count()
        if (targetBlockCount < 2) {
            throw GradleException("Unable to locate both iOS target buildSettings blocks in project.pbxproj.")
        }

        val syncedTargetBuildSettings = targetBuildSettingsBlockRegex.replace(pbxprojWithoutXcconfigReference) { matchResult ->
            var buildSettings = matchResult.groupValues[2]
            buildSettings = upsertIosBuildSetting(buildSettings, "DEVELOPMENT_TEAM", teamId)
            buildSettings = upsertIosBuildSetting(buildSettings, "PRODUCT_NAME", quotedProjectName)
            buildSettings = upsertIosBuildSetting(buildSettings, "INFOPLIST_KEY_CFBundleDisplayName", quotedAppName)
            buildSettings = upsertIosBuildSetting(buildSettings, "PRODUCT_BUNDLE_IDENTIFIER", appId)
            buildSettings = upsertIosBuildSetting(buildSettings, "MARKETING_VERSION", appVersionName)
            buildSettings = upsertIosBuildSetting(buildSettings, "CURRENT_PROJECT_VERSION", appVersionCode)
            "${matchResult.groupValues[1]}$buildSettings${matchResult.groupValues[3]}"
        }

        val appReferenceRegex = Regex(
            """(?m)^(\s*([A-F0-9]+)\s*/\* ).*?(\.app \*/ = \{isa = PBXFileReference; explicitFileType = wrapper\.application; includeInIndex = 0; path = ).*?(; sourceTree = BUILT_PRODUCTS_DIR; \};)$"""
        )
        val appReferenceMatch = appReferenceRegex.find(syncedTargetBuildSettings)
            ?: throw GradleException("Unable to locate iOS app PBXFileReference in project.pbxproj.")

        var updatedPbxproj = appReferenceRegex.replace(syncedTargetBuildSettings) { matchResult ->
            "${matchResult.groupValues[1]}$projectName${matchResult.groupValues[3]}$appProductFileName${matchResult.groupValues[4]}"
        }

        val appReferenceId = appReferenceMatch.groupValues[2]
        val appReferenceCommentRegex = Regex("""($appReferenceId /\* ).*?(\.app \*/)""")
        updatedPbxproj = updatedPbxproj.replace(appReferenceCommentRegex) { matchResult ->
            "${matchResult.groupValues[1]}$projectName${matchResult.groupValues[2]}"
        }

        val nativeTargetProductNameRegex = Regex("""(?m)^(\s*productName = ).*?;$""")
        if (!nativeTargetProductNameRegex.containsMatchIn(updatedPbxproj)) {
            throw GradleException("Unable to locate iOS native target productName in project.pbxproj.")
        }
        updatedPbxproj = updatedPbxproj.replace(nativeTargetProductNameRegex) { matchResult ->
            "${matchResult.groupValues[1]}$projectName;"
        }

        if (updatedPbxproj != pbxprojWithoutXcconfigReference) {
            iosProjectFile.writeText(updatedPbxproj)
        }

        println("Synced local.properties to iosApp/iosApp.xcodeproj/project.pbxproj")
    }
}

project(":shared").tasks.matching { it.name.contains("embedAndSign") }.configureEach {
    dependsOn("syncIosConfig")
}
