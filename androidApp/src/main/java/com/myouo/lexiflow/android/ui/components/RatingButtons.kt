package com.myouo.lexiflow.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RatingButtons(
    onAgainParams: () -> Unit,
    onHardParams: () -> Unit,
    onGoodParams: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val modifier = Modifier
            .weight(1f)
            .height(56.dp)

        Button(
            onClick = onAgainParams,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("不会")
        }
        
        Button(
            onClick = onHardParams,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF57C00) // Orange tone
            )
        ) {
            Text("模糊")
        }
        
        Button(
            onClick = onGoodParams,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("会")
        }
    }
}
