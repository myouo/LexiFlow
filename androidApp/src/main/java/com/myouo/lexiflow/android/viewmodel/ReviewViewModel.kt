package com.myouo.lexiflow.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myouo.lexiflow.database.DatabaseHelper
import com.myouo.lexiflow.database.Senses
import com.myouo.lexiflow.database.Words
import com.myouo.lexiflow.fsrs.Rating
import com.myouo.lexiflow.review.ReviewScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ReviewState(
    val word: Words? = null,
    val senses: List<Senses> = emptyList(),
    val currentSenseIndex: Int = 0,
    val isFinished: Boolean = false,
    val loading: Boolean = true,
    val empty: Boolean = false
)

class ReviewViewModel(
    private val db: DatabaseHelper,
    private val reviewScheduler: ReviewScheduler
) : ViewModel() {
    private val _state = MutableStateFlow(ReviewState())
    val state: StateFlow<ReviewState> = _state
    
    // In a real app we'd inject settings, hardcoding 3 for topK.
    private val topK = 3

    init {
        loadNextWord()
    }

    private fun loadNextWord() {
        viewModelScope.launch {
            android.util.Log.d("PerfLog", "ReviewViewModel state emitting (loadNextWord loading)")
            _state.value = _state.value.copy(loading = true)
            // Fetch next senses based on FSRS priority
            android.util.Log.d("PerfLog", "ReviewViewModel: DB getNextSensesToReview start")
            val nextSenses = withContext(Dispatchers.IO) { reviewScheduler.getNextSensesToReview(topK) }
            android.util.Log.d("PerfLog", "ReviewViewModel: DB getNextSensesToReview end")
            
            if (nextSenses.isEmpty()) {
                android.util.Log.d("PerfLog", "ReviewViewModel state emitting (loadNextWord empty)")
                _state.value = ReviewState(empty = true, isFinished = true, loading = false)
                return@launch
            }
            
            // For simplicity, we assume the scheduler returns `Limit` senses matching ONE word. 
            // Realistically, the scheduler would group senses by word. We just take the first word's senses for the UI mapping.
            val targetWordId = nextSenses.first().word_id
            android.util.Log.d("PerfLog", "ReviewViewModel: DB getWord start")
            val word = withContext(Dispatchers.IO) { db.getWord(targetWordId) }
            android.util.Log.d("PerfLog", "ReviewViewModel: DB getWord end")
            
            val sensesForWord = nextSenses.filter { it.word_id == targetWordId }.take(topK)
            
            android.util.Log.d("PerfLog", "ReviewViewModel state emitting (loadNextWord success)")
            _state.value = ReviewState(
                word = word,
                senses = sensesForWord,
                currentSenseIndex = 0,
                loading = false
            )
        }
    }

    fun submitRating(rating: Rating, durationMs: Long) {
        val currentState = _state.value
        if (currentState.isFinished || currentState.senses.isEmpty()) return
        
        viewModelScope.launch {
             val currentSense = currentState.senses[currentState.currentSenseIndex]
             android.util.Log.d("PerfLog", "ReviewViewModel: DB submitReview start")
             withContext(Dispatchers.IO) { 
                 reviewScheduler.submitReview(currentSense.id, rating, durationMs) 
             }
             android.util.Log.d("PerfLog", "ReviewViewModel: DB submitReview end")
             
             // Move to next sense
             if (currentState.currentSenseIndex < currentState.senses.size - 1) {
                 android.util.Log.d("PerfLog", "ReviewViewModel state emitting (submitRating next sense)")
                 _state.value = currentState.copy(
                     currentSenseIndex = currentState.currentSenseIndex + 1
                 )
             } else {
                 // Finished all senses, fetch next word
                 loadNextWord()
             }
        }
    }
}
