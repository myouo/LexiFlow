package com.myouo.lexiflow.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.myouo.lexiflow.android.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
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
                OutlinedTextField(
                    value = state.dailyTarget,
                    onValueChange = { viewModel.updateDailyTarget(it) },
                    label = { Text("每日新词目标") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = state.maxSenses,
                    onValueChange = { viewModel.updateMaxSenses(it) },
                    label = { Text("每词最大释义数") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = state.recallThreshold,
                    onValueChange = { viewModel.updateRecallThreshold(it) },
                    label = { Text("回忆阈值 (FSRS, 0.70-0.95)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
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
