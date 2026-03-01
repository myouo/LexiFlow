import sys
path="e:/LexiFlow/androidApp/src/main/java/com/myouo/lexiflow/android/ui/AnalyticsScreen.kt"
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

imp_old = "import com.myouo.lexiflow.android.viewmodel.AnalyticsViewModel"
imp_new = imp_old + "\nimport androidx.compose.foundation.background\nimport androidx.compose.foundation.clickable\nimport androidx.compose.foundation.interaction.MutableInteractionSource\nimport androidx.compose.animation.animateColorAsState\nimport androidx.compose.animation.core.animateFloatAsState\nimport androidx.compose.animation.core.FastOutSlowInEasing"
content = content.replace(imp_old, imp_new)

start_str = "val isTime = state.currentMetric == HeatmapMetric.TIME"
end_str = "Spacer(modifier = Modifier.height(16.dp))"
if start_str in content and end_str in content:
    start_idx = content.find(start_str)
    end_idx = content.find(end_str, start_idx)
    
    new_block = """MetricToggle(
                currentMetric = state.currentMetric,
                onToggle = { viewModel.toggleMetric() }
            )
        }
        
        """
    content = content[:start_idx] + new_block + content[end_idx:]

comp = '''
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
}'''

if "fun MetricToggle" not in content:
    content = content.rstrip() + "\n" + comp

with open(path, "w", encoding="utf-8") as f:
    f.write(content)
print("Updated via script")
