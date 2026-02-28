package com.myouo.lexiflow.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.myouo.lexiflow.analytics.HeatmapDay
import com.myouo.lexiflow.analytics.HeatmapMetric
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HeatmapGrid(
    days: List<HeatmapDay>,
    currentMetric: HeatmapMetric,
    modifier: Modifier = Modifier,
    clearTooltipTrigger: Int = 0
) {
    val cellSize = 14.dp
    val cellSpacingDp = 4.dp
    
    val scrollState = rememberScrollState()
    
    var clickedDayLabel by remember { mutableStateOf<String?>(null) }
    var clickedValueStr by remember { mutableStateOf<String?>(null) }
    var clickedCellBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var gridBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    
    val textMeasurer = rememberTextMeasurer()
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val titleStyle = MaterialTheme.typography.titleMedium
    val bodyStyle = MaterialTheme.typography.bodyMedium
    
    val dateMap = remember(days) { days.associateBy { it.date } }
    
    val today = remember { LocalDate.now() }
    val startDate = remember { today.minusDays(364) } // 365 total
    val leadingEmptyDays = remember { startDate.dayOfWeek.value % 7 } 
    val totalCols = remember { (365 + leadingEmptyDays + 6) / 7 }
    
    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.maxValue > 0) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }
    
    LaunchedEffect(currentMetric) {
        if (clickedDayLabel != null) {
            val stat = dateMap[clickedDayLabel]
            val value = stat?.tooltipValue ?: 0
            val prefix = if (currentMetric == HeatmapMetric.WORDS) "本日词数：" else "本日学习时长："
            val correctSuffix = if (currentMetric == HeatmapMetric.WORDS) " 个" else " 分钟"
            val valStr = if (value > 1000) "${value / 1000}k" else value.toString()
            clickedValueStr = "$prefix$valStr$correctSuffix"
        }
    }
    
    LaunchedEffect(clearTooltipTrigger) {
        if (clearTooltipTrigger > 0) {
            clickedDayLabel = null
        }
    }
    
    Box(
        modifier = modifier
            .onGloballyPositioned { gridBounds = it.boundsInWindow() }
            .pointerInput(Unit) {
                detectTapGestures { clickedDayLabel = null }
            }
            .drawWithContent {
                drawContent()
                if (clickedDayLabel != null && clickedValueStr != null && clickedCellBounds != null && gridBounds != null) {
                    val padding = 16.dp.toPx()
                    val spacing = 4.dp.toPx()
                    val cornerRadius = 8.dp.toPx()
                    
                    val titleResult = textMeasurer.measure(
                        text = clickedDayLabel!!,
                        style = titleStyle
                    )
                    val bodyResult = textMeasurer.measure(
                        text = clickedValueStr!!,
                        style = bodyStyle
                    )
                    
                    val tooltipWidth = maxOf(titleResult.size.width, bodyResult.size.width) + padding * 2
                    val tooltipHeight = titleResult.size.height + bodyResult.size.height + spacing + padding * 2
                    
                    val bounds = clickedCellBounds!!
                    val localLeft = bounds.left - gridBounds!!.left
                    val localRight = bounds.right - gridBounds!!.left
                    val localTop = bounds.top - gridBounds!!.top
                    val localBottom = bounds.bottom - gridBounds!!.top
                    
                    var x = localRight
                    var y = localBottom
                    
                    if (x + tooltipWidth > size.width) {
                        x = localLeft - tooltipWidth
                    }
                    if (y + tooltipHeight > size.height) {
                        y = localTop - tooltipHeight
                    }
                    
                    x = x.coerceIn(0f, maxOf(0f, size.width - tooltipWidth))
                    y = y.coerceIn(0f, maxOf(0f, size.height - tooltipHeight))
                    
                    drawRoundRect(
                        color = surfaceVariantColor.copy(alpha = 0.85f),
                        topLeft = Offset(x, y),
                        size = Size(tooltipWidth, tooltipHeight),
                        cornerRadius = CornerRadius(cornerRadius)
                    )
                    
                    drawText(
                        textLayoutResult = titleResult,
                        color = onSurfaceVariantColor,
                        topLeft = Offset(x + padding, y + padding)
                    )
                    drawText(
                        textLayoutResult = bodyResult,
                        color = onSurfaceVariantColor,
                        topLeft = Offset(x + padding, y + padding + titleResult.size.height + spacing)
                    )
                }
            }
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left Y-axis Labels
            Column(
                modifier = Modifier
                    .padding(top = 22.dp, end = 4.dp)
                    .width(28.dp),
                verticalArrangement = Arrangement.spacedBy(cellSpacingDp)
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEachIndexed { index, label ->
                    if (index == 1 || index == 3 || index == 5) { // Mon, Wed, Fri
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .height(cellSize)
                                .wrapContentHeight(Alignment.CenterVertically)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(cellSize))
                    }
                }
            }
            
            // Scrollable Grid
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
                    .padding(bottom = 8.dp, end = 8.dp)
            ) {
                // Month labels
                for (col in 0 until totalCols) {
                    val dayOffset = col * 7 - leadingEmptyDays
                    val firstDateOfCol = startDate.plusDays(maxOf(0, dayOffset).toLong())
                    
                    var hasFirstDay = false
                    for (i in 0 until 7) {
                        val idx = col * 7 - leadingEmptyDays + i
                        if (idx in 0..364) {
                            val d = startDate.plusDays(idx.toLong())
                            if (d.dayOfMonth == 1) hasFirstDay = true
                        }
                    }
                    
                    if (col == 0 || hasFirstDay) {
                        val monthName = firstDateOfCol.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                        Text(
                            text = monthName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .offset(x = ((col * (14 + 4)).dp), y = 0.dp)
                                .height(20.dp)
                        )
                    }
                }
                
                // Cells matrix
                Row(
                    modifier = Modifier.padding(top = 22.dp),
                    horizontalArrangement = Arrangement.spacedBy(cellSpacingDp)
                ) {
                    for (col in 0 until totalCols) {
                        Column(verticalArrangement = Arrangement.spacedBy(cellSpacingDp)) {
                            for (row in 0 until 7) {
                                val dayOffset = col * 7 + row - leadingEmptyDays
                                if (dayOffset < 0 || dayOffset >= 365) {
                                    Spacer(modifier = Modifier.size(cellSize))
                                } else {
                                    val currentDate = startDate.plusDays(dayOffset.toLong())
                                    val dateStr = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                    val stat = dateMap[dateStr]
                                    val level = stat?.level ?: 0
                                    
                                    val color = when(level) {
                                        1 -> Color(0xFF9BE9A8)
                                        2 -> Color(0xFF40C463)
                                        3 -> Color(0xFF30A14E)
                                        4 -> Color(0xFF216E39)
                                        else -> Color(0xFFEBEDF0)
                                    }
                                    
                                    var cellBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
                                    Box(
                                        modifier = Modifier
                                            .size(cellSize)
                                            .onGloballyPositioned { coords ->
                                                cellBounds = coords.boundsInWindow()
                                            }
                                            .background(color, RoundedCornerShape(2.dp))
                                            .clickable {
                                                clickedDayLabel = dateStr
                                                val value = stat?.tooltipValue ?: 0
                                                val prefix = if (currentMetric == HeatmapMetric.WORDS) "本日词数：" else "本日学习时长："
                                                val correctSuffix = if (currentMetric == HeatmapMetric.WORDS) " 个" else " 分钟"
                                                val valStr = if (value > 1000) "${value / 1000}k" else value.toString()
                                                clickedValueStr = "$prefix$valStr$correctSuffix"
                                                clickedCellBounds = cellBounds
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
    }
}
