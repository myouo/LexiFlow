package com.myouo.lexiflow.crypto

expect class CryptoManager {
    fun getOrCreateDeviceSecret(): String
    fun generateHmac(data: String, secret: String): String
}
