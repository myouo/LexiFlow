package com.myouo.lexiflow.crypto

actual class CryptoManager {
    actual fun getOrCreateDeviceSecret(): String {
        // TODO: Implement Keychain access in iOS 
        return "ios-dummy-secret"
    }

    actual fun generateHmac(data: String, secret: String): String {
        // TODO: Implement CommonCrypto HMAC in iOS
        return "ios-dummy-hmac"
    }
}
