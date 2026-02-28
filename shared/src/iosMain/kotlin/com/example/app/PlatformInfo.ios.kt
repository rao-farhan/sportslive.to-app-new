package com.example.app

import platform.UIKit.UIDevice

actual object PlatformInfo {
    actual val helloMessage: String = "Hello from iOS"
    actual val apiLevel: String = UIDevice.currentDevice.systemVersion
}
