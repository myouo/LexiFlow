package com.myouo.lexiflow.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue

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
        val modifierBase = Modifier
            .weight(1f)
            .height(56.dp)

        val againInteraction = remember { MutableInteractionSource() }
        val againPressed by againInteraction.collectIsPressedAsState()
        val againScale by animateFloatAsState(if (againPressed) 0.95f else 1f, tween(150))
        
        Button(
            onClick = onAgainParams,
            interactionSource = againInteraction,
            modifier = modifierBase.scale(againScale),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("不会")
        }
        
        val hardInteraction = remember { MutableInteractionSource() }
        val hardPressed by hardInteraction.collectIsPressedAsState()
        val hardScale by animateFloatAsState(if (hardPressed) 0.95f else 1f, tween(150))
        
        Button(
            onClick = onHardParams,
            interactionSource = hardInteraction,
            modifier = modifierBase.scale(hardScale),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF57C00) // Orange tone
            )
        ) {
            Text("模糊")
        }
        
        val goodInteraction = remember { MutableInteractionSource() }
        val goodPressed by goodInteraction.collectIsPressedAsState()
        val goodScale by animateFloatAsState(if (goodPressed) 0.95f else 1f, tween(150))
        
        Button(
            onClick = onGoodParams,
            interactionSource = goodInteraction,
            modifier = modifierBase.scale(goodScale),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("会")
        }
    }
}
