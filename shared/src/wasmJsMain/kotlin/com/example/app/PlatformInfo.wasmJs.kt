package com.example.app

actual object PlatformInfo {
    actual val helloMessage: String = "Hello from Wasm"
    actual val apiLevel: String = "N/A"
}
