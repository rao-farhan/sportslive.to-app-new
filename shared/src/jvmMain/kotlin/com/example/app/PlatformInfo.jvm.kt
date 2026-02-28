package com.example.app

actual object PlatformInfo {
    actual val helloMessage: String = "Hello from JVM"
    actual val apiLevel: String = System.getProperty("java.version") ?: "N/A"
}
