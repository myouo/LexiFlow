package com.myouo.lexiflow.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myouo.lexiflow.analytics.HeatmapCalculator
import com.myouo.lexiflow.analytics.HeatmapDay
import com.myouo.lexiflow.analytics.HeatmapMetric
import com.myouo.lexiflow.database.DatabaseHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AnalyticsState(
    val heatmapDays: List<HeatmapDay> = emptyList(),
    val currentMetric: HeatmapMetric = HeatmapMetric.WORDS,
    val totalStudyDays: Int = 0,
    val currentStreak: Int = 0,
    val totalStudySeconds: Long = 0L
)

class AnalyticsViewModel(
    private val heatmapCalculator: HeatmapCalculator,
    private val db: DatabaseHelper
) : ViewModel() {
    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state

    init {
        android.util.Log.d("PerfLog", "AnalyticsViewModel initialized (Single Init)")
        loadData()
    }

    fun toggleMetric() {
        android.util.Log.d("PerfLog", "AnalyticsViewModel state emitting (toggleMetric)")
        val newMetric = if (_state.value.currentMetric == HeatmapMetric.WORDS) HeatmapMetric.TIME else HeatmapMetric.WORDS
        _state.value = _state.value.copy(currentMetric = newMetric)
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            android.util.Log.d("PerfLog", "AnalyticsViewModel: HeatmapCalc start")
            val days = withContext(Dispatchers.IO) { heatmapCalculator.getHeatmapData(_state.value.currentMetric) }
            android.util.Log.d("PerfLog", "AnalyticsViewModel: HeatmapCalc end")
            
            // Pad empty year if no stats
            val displayDays = if (days.isEmpty()) {
                List(365) { HeatmapDay("", 0, 0) }
            } else {
                days
            }

            // Calculate total time
            val allStats = withContext(Dispatchers.IO) { db.queries().getAllDailyStats().executeAsList() }
            val totalSeconds = allStats.sumOf { it.focus_seconds.toLong() }

            android.util.Log.d("PerfLog", "AnalyticsViewModel state emitting (loadData)")
            _state.value = _state.value.copy(
                heatmapDays = displayDays,
                totalStudyDays = displayDays.count { it.level > 0 },
                currentStreak = calculateStreak(displayDays),
                totalStudySeconds = totalSeconds
            )
        }
    }
    
    private fun calculateStreak(days: List<HeatmapDay>): Int {
        var streak = 0
        for (i in days.indices.reversed()) {
            if (days[i].level > 0) streak++ else break
        }
        return streak
    }
}
