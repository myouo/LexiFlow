package com.myouo.lexiflow.android.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("LexiFlow Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("1-Year Learning Heatmap", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        // GitHub-Style Heatmap Canvas
        Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            val cols = 52
            val rows = 7
            val cellSize = 12f
            val spacing = 4f
            
            for (c in 0 until cols) {
                for (r in 0 until rows) {
                    val x = c * (cellSize + spacing)
                    val y = r * (cellSize + spacing)
                    val level = (Math.random() * 5).toInt()
                    
                    val color = when(level) {
                        0 -> Color(0xFFEBEDF0)
                        1 -> Color(0xFF9BE9A8)
                        2 -> Color(0xFF40C463)
                        3 -> Color(0xFF30A14E)
                        else -> Color(0xFF216E39)
                    }
                    
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(cellSize, cellSize),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                }
            }
        }
    }
}
