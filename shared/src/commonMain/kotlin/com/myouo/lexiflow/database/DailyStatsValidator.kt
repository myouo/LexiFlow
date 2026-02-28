package com.myouo.lexiflow.database

import com.myouo.lexiflow.crypto.CryptoManager

class DailyStatsValidator(private val cryptoManager: CryptoManager) {
    
    fun generateSignature(date: String, focusSeconds: Int, newLearned: Int, reviews: Int): String {
        val secret = cryptoManager.getOrCreateDeviceSecret()
        val data = "$date|$focusSeconds|$newLearned|$reviews"
        return cryptoManager.generateHmac(data, secret)
    }
    
    fun isValid(stats: Daily_stats): Boolean {
        val expectedSig = generateSignature(stats.date, stats.focus_seconds, stats.new_learned_words, stats.reviews)
        return expectedSig == stats.sig
    }
}
