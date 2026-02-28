package com.myouo.lexiflow.analytics

import com.myouo.lexiflow.database.DatabaseHelper
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class HeatmapMetric {
    TIME,
    WORDS
}

data class HeatmapDay(val date: String, val level: Int, val tooltipValue: Int)

class HeatmapCalculator(private val db: DatabaseHelper) {
    
    suspend fun getHeatmapData(metric: HeatmapMetric): List<HeatmapDay> {
        val allStats = db.queries().getAllDailyStats().executeAsList()
        
        // Filter non-zero values for quantile calc
        val nonZeroValues = allStats.map { stat ->
            when (metric) {
                HeatmapMetric.TIME -> stat.focus_seconds / 60 // convert to minutes as requested
                HeatmapMetric.WORDS -> stat.new_learned_words
            }
        }.filter { it > 0 }.sorted()
        
        val thresholds = calculateQuantiles(nonZeroValues)
        
        return allStats.map { stat ->
            val value = when (metric) {
                HeatmapMetric.TIME -> stat.focus_seconds / 60
                HeatmapMetric.WORDS -> stat.new_learned_words
            }
            
            val level = if (value == 0) 0 else getLevel(value, thresholds)
            HeatmapDay(stat.date, level, value)
        }
    }
    
    private fun calculateQuantiles(sortedValues: List<Int>): List<Int> {
        if (sortedValues.isEmpty()) return listOf(0, 0, 0)
        
        val n = sortedValues.size
        if (n < 4) {
            // fallback if too few days
            val maxVal = sortedValues.maxOrNull() ?: 0
            return listOf(maxVal / 4, maxVal / 2, maxVal * 3 / 4)
        }
        
        val q1 = sortedValues[(n * 0.25).toInt()]
        val q2 = sortedValues[(n * 0.50).toInt()]
        val q3 = sortedValues[(n * 0.75).toInt()]
        
        // Ensure no collapsed identical quantiles (force distinctiveness for UI)
        return listOf(q1, q2, q3)
    }
    
    private fun getLevel(value: Int, thresholds: List<Int>): Int {
        val q1 = thresholds[0]
        val q2 = thresholds[1]
        val q3 = thresholds[2]
        
        if (q1 == q3 && q1 != 0) {
            // collapsed quantiles (e.g. all days are EXACTLY 10 mins). Just return 2.
            return if (value < q1) 1 else if (value == q1) 2 else 4
        }
        
        return when {
            value <= q1 -> 1
            value <= q2 -> 2
            value <= q3 -> 3
            else -> 4
        }
    }
    
    fun getTodayString(): String {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val yy = today.year.toString().padStart(4, '0')
        val mm = today.monthNumber.toString().padStart(2, '0')
        val dd = today.dayOfMonth.toString().padStart(2, '0')
        return "$yy-$mm-$dd"
    }
}
