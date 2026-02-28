package com.myouo.lexiflow.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.myouo.lexiflow.android.ui.components.RatingButtons
import com.myouo.lexiflow.android.ui.components.SenseChipRow
import com.myouo.lexiflow.android.viewmodel.ReviewViewModel
import com.myouo.lexiflow.fsrs.Rating
// Note: In real setup, ViewModel is injected via Koin/Hilt or Factory.
// For scaffolding clarity, assume state is hoisted correctly.

@Composable
fun ReviewScreen(navController: NavController, viewModel: ReviewViewModel) {
    val reviewState = viewModel.state.collectAsState().value
    
    // Empty state logic
    if (reviewState.empty) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("没有待复习的词汇。", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("vocabulary") }) {
                Text("去导入词库")
            }
        }
        return
    }

    if (reviewState.loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentSense = reviewState.senses.getOrNull(reviewState.currentSenseIndex)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top: Word Details
        Text(
            text = reviewState.word?.lemma ?: "未知词汇",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // POS Chip
        SuggestionChip(
            onClick = {},
            label = { Text(currentSense?.pos ?: "词汇属性") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Middle Scrollable Area: Definition
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                "释义：", 
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentSense?.gloss ?: "暂无释义。",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        
        // Sense Chip Nav
        if (reviewState.senses.size > 1) {
            Text(
                text = "${reviewState.currentSenseIndex + 1} / ${reviewState.senses.size} 个释义",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            SenseChipRow(
                totalSenses = reviewState.senses.size,
                currentIndex = reviewState.currentSenseIndex,
                onChipSelected = { /* ViewModel logic would intercept jump if needed */ }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Rating Buttons
        RatingButtons(
            onAgainParams = { viewModel.submitRating(Rating.AGAIN, 3000L) },
            onHardParams = { viewModel.submitRating(Rating.HARD, 3000L) },
            onGoodParams = { viewModel.submitRating(Rating.GOOD, 3000L) }
        )
    }
}
