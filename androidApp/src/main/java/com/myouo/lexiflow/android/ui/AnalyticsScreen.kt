package com.myouo.lexiflow.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Rect
import com.myouo.lexiflow.analytics.HeatmapMetric
import com.myouo.lexiflow.android.ui.components.HeatmapGrid
import com.myouo.lexiflow.android.viewmodel.AnalyticsViewModel

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val state = viewModel.state.collectAsState().value
    
    var clearTooltipTrigger by remember { mutableStateOf(0) }
    var heatmapBounds by remember { mutableStateOf<Rect?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (heatmapBounds != null && !heatmapBounds!!.contains(offset)) {
                        clearTooltipTrigger++
                    }
                }
            }
            .padding(16.dp)
    ) {
        Text("学习统计", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        // Top: Metric Toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text("过去一年记录", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            
            val isTime = state.currentMetric == HeatmapMetric.TIME
            FilledTonalButton(onClick = { viewModel.toggleMetric() }) {
                Text(if (isTime) "指标: 学习时长" else "指标: 学习词数")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Heatmap Grid
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .onGloballyPositioned { coords ->
                    heatmapBounds = coords.boundsInParent()
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                HeatmapGrid(
                    days = state.heatmapDays,
                    currentMetric = state.currentMetric,
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    clearTooltipTrigger = clearTooltipTrigger
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Below Heatmap Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                label = "学习天数", 
                value = state.totalStudyDays.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "当前连续打卡", 
                value = "${state.currentStreak} 天",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val totalSecs = state.totalStudySeconds
        val hours = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "总学习时长",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "${hours} 小时 ${mins} 分",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
