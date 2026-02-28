package com.myouo.lexiflow.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.myouo.lexiflow.analytics.HeatmapDay
import com.myouo.lexiflow.analytics.HeatmapMetric
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

@Composable
fun HeatmapGrid(
    days: List<HeatmapDay>,
    currentMetric: HeatmapMetric,
    modifier: Modifier = Modifier
) {
    val cellSize = 14.dp
    val cellSpacingDp = 4.dp
    
    val scrollState = rememberScrollState()
    
    var clickedDayLabel by remember { mutableStateOf<String?>(null) }
    var clickedValueStr by remember { mutableStateOf<String?>(null) }
    var clickedCellBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    
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
    
    Box(modifier = modifier) {
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
                                                val suffix = if (currentMetric == HeatmapMetric.WORDS) " 分钟" else " 个"
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
        
        if (clickedDayLabel != null && clickedCellBounds != null) {
            Popup(
                popupPositionProvider = object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize
                    ): IntOffset {
                        val bounds = clickedCellBounds!!
                        var x = bounds.right.toInt()
                        var y = bounds.bottom.toInt()
                        
                        // Flip left if offscreen right
                        if (x + popupContentSize.width > windowSize.width) {
                            x = bounds.left.toInt() - popupContentSize.width
                        }
                        // Flip up if offscreen bottom
                        if (y + popupContentSize.height > windowSize.height) {
                            y = bounds.top.toInt() - popupContentSize.height
                        }
                        
                        // Coerce gracefully mapping window sizes
                        x = x.coerceIn(0, maxOf(0, windowSize.width - popupContentSize.width))
                        y = y.coerceIn(0, maxOf(0, windowSize.height - popupContentSize.height))
                        
                        return IntOffset(x, y)
                    }
                },
                onDismissRequest = { clickedDayLabel = null }
            ) {
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = clickedDayLabel!!,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = clickedValueStr!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                }
            }
        }
    }
}
