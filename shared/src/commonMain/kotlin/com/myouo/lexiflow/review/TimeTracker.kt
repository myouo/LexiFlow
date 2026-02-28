package com.myouo.lexiflow.review

import kotlinx.datetime.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TimeTracker {
    private var lastInteractionTime = Clock.System.now().toEpochMilliseconds()
    private val _effectiveTimeMs = MutableStateFlow(0L)
    val effectiveTimeMs: StateFlow<Long> = _effectiveTimeMs
    
    fun onInteraction() {
        val now = Clock.System.now().toEpochMilliseconds()
        val diff = now - lastInteractionTime
        
        // Only count if gap is less than 30 seconds
        if (diff < 30_000) {
            _effectiveTimeMs.value += diff
        }
        
        lastInteractionTime = now
    }
    
    fun onBackground() {
        // Paused logic
        lastInteractionTime = 0L // invalidate diff on return
    }
    
    fun onForeground() {
        lastInteractionTime = Clock.System.now().toEpochMilliseconds()
    }
}
