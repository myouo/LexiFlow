package com.myouo.lexiflow.android.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val recomposeCount = remember { arrayOf(0) }
    androidx.compose.runtime.SideEffect {
        recomposeCount[0]++
        android.util.Log.d("PerfLog", "AnalyticsScreen recomposed: ${recomposeCount[0]}")
    }
    val state = viewModel.state.collectAsState().value
    
    var clearTooltipTrigger by remember { mutableStateOf(0) }
    val heatmapBounds = remember { arrayOf<Rect?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (heatmapBounds[0] != null && !heatmapBounds[0]!!.contains(offset)) {
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
            
            MetricToggle(
                currentMetric = state.currentMetric,
                onToggle = { viewModel.toggleMetric() }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Heatmap Grid
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .onGloballyPositioned { coords ->
                    heatmapBounds[0] = coords.boundsInParent()
                },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                    AnimatedContent(
                        targetState = state.currentMetric,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(150)) togetherWith fadeOut(animationSpec = tween(150))
                        }
                    ) { metric ->
                        HeatmapGrid(
                            days = state.heatmapDays,
                            currentMetric = metric,
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            clearTooltipTrigger = clearTooltipTrigger
                        )
                    }
                }
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
            // Animate number if it's strictly numeric
            val numericValue = value.filter { it.isDigit() }.toIntOrNull()
            if (numericValue != null) {
                val animatedNum by androidx.compose.animation.core.animateIntAsState(
                    targetValue = numericValue,
                    animationSpec = androidx.compose.animation.core.tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                )
                val suffix = value.filter { !it.isDigit() }.trim()
                val displayStr = if (suffix.isNotEmpty()) "$animatedNum $suffix" else animatedNum.toString()
                Text(displayStr, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            } else {
                Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun MetricToggle(
    currentMetric: HeatmapMetric,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTime = currentMetric == HeatmapMetric.TIME
    
    val indicatorOffsetFraction by animateFloatAsState(
        targetValue = if (isTime) 1f else 0f,
        animationSpec = tween(
            durationMillis = 200, 
            easing = FastOutSlowInEasing
        )
    )

    val wordsTextColor by animateColorAsState(
        targetValue = if (!isTime) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200)
    )
    val timeTextColor by animateColorAsState(
        targetValue = if (isTime) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200)
    )

    Surface(
        shape = androidx.compose.foundation.shape.CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .height(40.dp)
            .width(200.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val indicatorWidth = maxWidth / 2
            val indicatorOffset = (maxWidth / 2) * indicatorOffsetFraction
            
            // Sliding Indicator Box
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(indicatorWidth)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
            
            // Labels Row
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { if (isTime) onToggle() },
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "学习词数",
                        color = wordsTextColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { if (!isTime) onToggle() },
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "学习时长",
                        color = timeTextColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}