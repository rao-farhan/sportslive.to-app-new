package to.sports.live.util

import to.sports.live.BuildKonfig

object Secrets {
    val baseUrl: String = BuildKonfig.BASE_URL
    val appId: String = BuildKonfig.APP_ID
    val versionName: String = BuildKonfig.APP_VERSION_NAME
    val versionCode: String = BuildKonfig.APP_VERSION_CODE

    val apiKey: String by lazy {
        BuildKonfig.ENCODED_API_KEY.map {
            (it.code xor BuildKonfig.XOR_KEY).toChar()
        }.joinToString("")
    }
}
