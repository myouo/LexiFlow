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
import androidx.compose.ui.graphics.drawscope.withTransform
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
    android.util.Log.d("PerfLog", "HeatmapGrid recomposing")
    val cellSize = 14.dp
    val cellSpacingDp = 4.dp
    
    val scrollState = rememberScrollState()
    
    var clickedDayLabel by remember { mutableStateOf<String?>(null) }
    var clickedValueStr by remember { mutableStateOf<String?>(null) }
    var clickedCol by remember { mutableStateOf(-1) }
    var clickedRow by remember { mutableStateOf(-1) }
    var rootCoords: androidx.compose.ui.layout.LayoutCoordinates? = null
    var canvasCoords: androidx.compose.ui.layout.LayoutCoordinates? = null
    
    val textMeasurer = rememberTextMeasurer()
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val titleStyle = MaterialTheme.typography.titleMedium
    val bodyStyle = MaterialTheme.typography.bodyMedium
    
    var activeTooltipData by remember { mutableStateOf<Array<Any>?>(null) }
    
    val tooltipAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (clickedDayLabel != null) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(150, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    )
    val tooltipScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (clickedDayLabel != null) 1f else 0.95f,
        animationSpec = androidx.compose.animation.core.tween(150, easing = androidx.compose.animation.core.FastOutSlowInEasing)
    )
    
    val dateMap = remember(days) { days.associateBy { it.date } }
    
    val today = remember { LocalDate.now() }
    val startDate = remember { today.minusDays(364) } // 365 total
    val leadingEmptyDays = remember { startDate.dayOfWeek.value % 7 } 
    val totalCols = remember { (365 + leadingEmptyDays + 6) / 7 }
    
    // Precompute the 365 date strings outside the render loop
    val precomputedDates = remember(startDate) {
        val arr = Array<String?>(totalCols * 7) { null }
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        for (col in 0 until totalCols) {
            for (row in 0 until 7) {
                val dayOffset = col * 7 + row - leadingEmptyDays
                if (dayOffset in 0..364) {
                    val date = startDate.plusDays(dayOffset.toLong())
                    arr[col * 7 + row] = date.format(formatter)
                }
            }
        }
        arr
    }
    
    val monthLabels = remember(startDate) {
        val labels = mutableListOf<Pair<Int, String>>()
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
                labels.add(col to firstDateOfCol.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
            }
        }
        labels
    }
    
    LaunchedEffect(Unit) {
        androidx.compose.runtime.snapshotFlow { scrollState.maxValue }
            .collect { maxVal ->
                if (maxVal > 0 && scrollState.value == 0) { // Only scroll on initial load
                    scrollState.scrollTo(maxVal)
                }
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
            .onGloballyPositioned { rootCoords = it }
            .pointerInput(Unit) {
                detectTapGestures { clickedDayLabel = null }
            }
            .drawWithContent {
                val currentScroll = scrollState.value // Re-draw on scroll
                drawContent()
                
                // 1. Update data if visible
                if (clickedDayLabel != null && clickedValueStr != null && clickedCol >= 0 && clickedRow >= 0 && rootCoords != null && canvasCoords != null) {
                    val canvasBounds = try {
                        rootCoords!!.localBoundingBoxOf(canvasCoords!!)
                    } catch (e: Exception) { return@drawWithContent }
                    
                    val cellStepX = (cellSize + cellSpacingDp).toPx()
                    val cellStepY = (cellSize + cellSpacingDp).toPx()
                    val canvasTopPadding = 22.dp.toPx()
                    
                    val cellX = clickedCol * cellStepX
                    val cellY = canvasTopPadding + clickedRow * cellStepY
                    val cellRect = androidx.compose.ui.geometry.Rect(cellX, cellY, cellX + cellSize.toPx(), cellY + cellSize.toPx())
                    val bounds = cellRect.translate(canvasBounds.topLeft)
                    
                    val padding = 16.dp.toPx()
                    val spacing = 4.dp.toPx()
                    val gap = 8.dp.toPx()
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
                    
                    val isUpperHalf = clickedRow < 3
                    
                    var x = bounds.right + gap
                    var y = if (isUpperHalf) bounds.bottom + gap else bounds.top - tooltipHeight - gap
                    
                    if (x + tooltipWidth > size.width) {
                        x = bounds.left - tooltipWidth - gap
                    }
                    
                    x = x.coerceIn(0f, maxOf(0f, size.width - tooltipWidth))
                    y = y.coerceIn(0f, maxOf(0f, size.height - tooltipHeight))
                    
                    activeTooltipData = arrayOf(x, y, tooltipWidth, tooltipHeight, titleResult, bodyResult)
                }
                
                // 2. Render from cached data block applying animation scale/alpha independently
                if (activeTooltipData != null && tooltipAlpha > 0f) {
                    val d = activeTooltipData!!
                    val x = d[0] as Float
                    val y = d[1] as Float
                    val tw = d[2] as Float
                    val th = d[3] as Float
                    val tRes = d[4] as androidx.compose.ui.text.TextLayoutResult
                    val bRes = d[5] as androidx.compose.ui.text.TextLayoutResult
                    
                    val padding = 16.dp.toPx()
                    val spacing = 4.dp.toPx()
                    val cornerRadius = 8.dp.toPx()
                    
                    withTransform({
                        scale(tooltipScale, tooltipScale, pivot = Offset(x + tw / 2, y + th))
                    }) {
                        drawRoundRect(
                            color = surfaceVariantColor.copy(alpha = 0.85f * tooltipAlpha),
                            topLeft = Offset(x, y),
                            size = Size(tw, th),
                            cornerRadius = CornerRadius(cornerRadius)
                        )
                        drawText(
                            textLayoutResult = tRes,
                            color = onSurfaceVariantColor.copy(alpha = tooltipAlpha),
                            topLeft = Offset(x + padding, y + padding)
                        )
                        drawText(
                            textLayoutResult = bRes,
                            color = onSurfaceVariantColor.copy(alpha = tooltipAlpha),
                            topLeft = Offset(x + padding, y + padding + tRes.size.height + spacing)
                        )
                    }
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
                val labelStyle = MaterialTheme.typography.labelSmall
                
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .size(
                            width = (cellSize + cellSpacingDp) * totalCols,
                            height = 22.dp + (cellSize + cellSpacingDp) * 7
                        )
                        .onGloballyPositioned { canvasCoords = it }
                        .pointerInput(currentMetric) {
                            detectTapGestures { offset ->
                                val cellStepX = (cellSize + cellSpacingDp).toPx()
                                val cellStepY = (cellSize + cellSpacingDp).toPx()
                                val topPadding = 22.dp.toPx()
                                
                                val col = (offset.x / cellStepX).toInt()
                                val row = ((offset.y - topPadding) / cellStepY).toInt()
                                
                                if (col in 0 until totalCols && row in 0 until 7 && offset.y >= topPadding) {
                                    val dayOffset = col * 7 + row - leadingEmptyDays
                                    val dateStr = precomputedDates[col * 7 + row]
                                    if (dayOffset in 0..364 && dateStr != null) {
                                        clickedDayLabel = dateStr
                                        val stat = dateMap[dateStr]
                                        val value = stat?.tooltipValue ?: 0
                                        val prefix = if (currentMetric == HeatmapMetric.WORDS) "本日词数：" else "本日学习时长："
                                        val correctSuffix = if (currentMetric == HeatmapMetric.WORDS) " 个" else " 分钟"
                                        val valStr = if (value > 1000) "${value / 1000}k" else value.toString()
                                        clickedValueStr = "$prefix$valStr$correctSuffix"
                                        clickedCol = col
                                        clickedRow = row
                                    } else {
                                        clickedDayLabel = null
                                    }
                                } else {
                                    clickedDayLabel = null
                                }
                            }
                        }
                ) {
                    monthLabels.forEach { (col, monthName) ->
                        val textLayoutResult = textMeasurer.measure(
                            text = monthName,
                            style = labelStyle
                        )
                        drawText(
                            textLayoutResult = textLayoutResult,
                            color = onSurfaceVariantColor,
                            topLeft = Offset(col * (cellSize + cellSpacingDp).toPx(), 0f)
                        )
                    }
                    
                    val color1 = Color(0xFF9BE9A8)
                    val color2 = Color(0xFF40C463)
                    val color3 = Color(0xFF30A14E)
                    val color4 = Color(0xFF216E39)
                    val color0 = Color(0xFFEBEDF0)
                    
                    val cellStepX = (cellSize + cellSpacingDp).toPx()
                    val cellStepY = (cellSize + cellSpacingDp).toPx()
                    val topPadding = 22.dp.toPx()
                    val cellSizePx = cellSize.toPx()
                    val cornerRadius = CornerRadius(2.dp.toPx())
                    
                    for (col in 0 until totalCols) {
                        for (row in 0 until 7) {
                            val dayOffset = col * 7 + row - leadingEmptyDays
                            val dateStr = precomputedDates[col * 7 + row]
                            if (dayOffset in 0..364 && dateStr != null) {
                                val stat = dateMap[dateStr]
                                val level = stat?.level ?: 0
                                
                                val color = when(level) {
                                    1 -> color1
                                    2 -> color2
                                    3 -> color3
                                    4 -> color4
                                    else -> color0
                                }
                                
                                drawRoundRect(
                                    color = color,
                                    topLeft = Offset(col * cellStepX, topPadding + row * cellStepY),
                                    size = Size(cellSizePx, cellSizePx),
                                    cornerRadius = cornerRadius
                                )
                            }
                        }
                    }
                }
            }
        }
        
    }
}
