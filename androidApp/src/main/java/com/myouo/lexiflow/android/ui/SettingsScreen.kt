package com.myouo.lexiflow.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import com.myouo.lexiflow.android.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val recomposeCount = remember { arrayOf(0) }
    androidx.compose.runtime.SideEffect {
        recomposeCount[0]++
        android.util.Log.d("PerfLog", "SettingsScreen recomposed: ${recomposeCount[0]}")
    }
    val state = viewModel.state.collectAsState().value
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("设置", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Theme
        item {
            Text("外观", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            ListItem(
                headlineContent = { Text("主题") },
                supportingContent = { Text(state.theme) }
            )
            ListItem(
                headlineContent = { Text("语言") },
                supportingContent = { Text(state.language) }
            )
            Divider()
        }
        
        // Learning
        item {
            Text("学习配置", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                var dailyTargetInput by remember(state.dailyTarget) { mutableStateOf(state.dailyTarget) }
                OutlinedTextField(
                    value = dailyTargetInput,
                    onValueChange = { dailyTargetInput = it },
                    label = { Text("每日新词目标") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.updateDailyTarget(dailyTargetInput) }),
                    modifier = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused) viewModel.updateDailyTarget(dailyTargetInput) }
                )
            }
            
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                var maxSensesInput by remember(state.maxSenses) { mutableStateOf(state.maxSenses) }
                OutlinedTextField(
                    value = maxSensesInput,
                    onValueChange = { maxSensesInput = it },
                    label = { Text("每词最大释义数") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.updateMaxSenses(maxSensesInput) }),
                    modifier = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused) viewModel.updateMaxSenses(maxSensesInput) }
                )
            }
            
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                var recallThresholdInput by remember(state.recallThreshold) { mutableStateOf(state.recallThreshold) }
                OutlinedTextField(
                    value = recallThresholdInput,
                    onValueChange = { recallThresholdInput = it },
                    label = { Text("回忆阈值 (FSRS, 0.70-0.95)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.updateRecallThreshold(recallThresholdInput) }),
                    modifier = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused) viewModel.updateRecallThreshold(recallThresholdInput) }
                )
            }

            ListItem(
                headlineContent = { Text("随机学习顺序") },
                trailingContent = {
                    Switch(checked = state.isRandomOrder, onCheckedChange = { viewModel.toggleRandomOrder(it) })
                }
            )
            Divider()
        }
        
        // Backup
        item {
            Text("备份与恢复", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceAround) {
                OutlinedButton(onClick = { viewModel.exportBackup() }) { Text("导出备份") }
                OutlinedButton(onClick = { viewModel.importBackup() }) { Text("导入备份") }
            }
        }
    }
}
