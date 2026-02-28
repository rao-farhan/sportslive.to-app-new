package com.example.app

import android.os.Build

actual object PlatformInfo {
    actual val helloMessage: String = "Hello from Android"
    actual val apiLevel: String = Build.VERSION.SDK_INT.toString()
}
