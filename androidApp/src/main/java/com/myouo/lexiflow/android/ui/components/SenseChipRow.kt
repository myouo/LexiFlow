package com.myouo.lexiflow.android.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SenseChipRow(
    totalSenses: Int,
    currentIndex: Int,
    onChipSelected: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(totalSenses) { index ->
            FilterChip(
                selected = index == currentIndex,
                onClick = { onChipSelected(index) },
                label = { Text("Sense \${index + 1}") },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}
