package com.myouo.lexiflow.fsrs

import kotlinx.datetime.Clock
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.min

enum class Rating(val value: Int) {
    AGAIN(0),
    HARD(1),
    GOOD(2)
}

data class FSRSItem(
    val stability: Double,
    val difficulty: Double,
    val lapses: Int,
    val due: Long,
    val lastReviewAt: Long
)

class FSRS {
    // Default FSRS v4 parameters (simplified mapping for 3 options)
    private val w = doubleArrayOf(
        0.4, 0.6, 2.4, 5.8, 4.93, 0.94, 0.86, 0.01, 1.49, 0.14, 0.94, 2.18, 0.05, 0.34, 1.26, 0.29, 2.61,
    )

    fun calculateNewState(rating: Rating, currentItem: FSRSItem?): FSRSItem {
        val now = Clock.System.now().toEpochMilliseconds()
        
        if (currentItem == null) {
            val initDifficulty = w[4] - w[5] * (rating.value - 2.0)
            val difficulty = clampDifficulty(initDifficulty)
            val stability = max(w[rating.value], 0.1)
            val interval = (stability * 24 * 60 * 60 * 1000).toLong() 
            
            return FSRSItem(
                stability = stability,
                difficulty = difficulty,
                lapses = if (rating == Rating.AGAIN) 1 else 0,
                due = now + interval,
                lastReviewAt = now
            )
        }

        val elapsedDays = (now - currentItem.lastReviewAt) / (24.0 * 60 * 60 * 1000)
        val currentDifficulty = currentItem.difficulty
        val currentStability = currentItem.stability
        val retrievability = exp(ln(0.9) * elapsedDays / currentStability)

        val nextDifficulty = clampDifficulty(currentDifficulty - w[6] * (rating.value - 2.0))
        
        val nextStability = if (rating == Rating.AGAIN) {
             w[11] * currentDifficulty.pow(-w[12]) * currentStability.pow(w[13]) * exp((1 - retrievability) * w[14])
        } else {
            val h = if (rating == Rating.HARD) w[15] else 1.0
            currentStability * (1 + exp(w[8]) *
                    (11 - nextDifficulty) *
                    currentStability.pow(-w[9]) *
                    (exp((1 - retrievability) * w[10]) - 1) * h)
        }

        val lapses = currentItem.lapses + if (rating == Rating.AGAIN) 1 else 0
        val interval = (nextStability * 24 * 60 * 60 * 1000).toLong()

        return FSRSItem(
            stability = nextStability,
            difficulty = nextDifficulty,
            lapses = lapses,
            due = now + interval,
            lastReviewAt = now
        )
    }

    private fun clampDifficulty(difficulty: Double): Double {
        return max(1.0, min(10.0, difficulty))
    }
}
