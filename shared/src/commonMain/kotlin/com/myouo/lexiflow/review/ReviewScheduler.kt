package com.myouo.lexiflow.review

import com.myouo.lexiflow.database.DatabaseHelper
import com.myouo.lexiflow.fsrs.FSRS
import com.myouo.lexiflow.fsrs.Rating
import com.myouo.lexiflow.fsrs.FSRSItem
import com.myouo.lexiflow.database.Senses
import kotlinx.datetime.Clock

class ReviewScheduler(private val db: DatabaseHelper) {
    private val fsrs = FSRS()
    
    suspend fun getNextSensesToReview(limit: Int): List<Senses> {
        val now = Clock.System.now().toEpochMilliseconds()
        
        // Priority 1: Due
        val dueSenses = db.queries().getSensesDue(now).executeAsList()
        val dueToReview = dueSenses.take(limit)
        
        if (dueToReview.size >= limit) return dueToReview
        
        val newNeeded = limit - dueToReview.size
        
        // Priority 2/3: New Senses
        val newSenses = db.queries().getNewSenses(newNeeded.toLong()).executeAsList()
        
        return dueToReview + newSenses
    }
    
    suspend fun submitReview(senseId: String, rating: Rating, durationMs: Long) {
        val state = db.queries().getSrsState(senseId).executeAsOneOrNull()
        
        val fsrsItem = state?.let {
            FSRSItem(it.stability, it.difficulty, it.lapses, it.due, it.last_review_at)
        }
        
        val newState = fsrs.calculateNewState(rating, fsrsItem)
        
        db.queries().transaction {
            db.queries().insertSrsState(
                sense_id = senseId,
                stability = newState.stability,
                difficulty = newState.difficulty,
                due = newState.due,
                last_review_at = newState.lastReviewAt,
                lapses = newState.lapses
            )
            
            val timestamp = Clock.System.now().toEpochMilliseconds()
            db.queries().insertReviewLog(
                sense_id = senseId,
                rating = rating.value,
                timestamp = timestamp,
                duration_ms = durationMs
            )
        }
    }
}
