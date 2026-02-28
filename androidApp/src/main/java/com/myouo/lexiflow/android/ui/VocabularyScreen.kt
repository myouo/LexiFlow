package com.myouo.lexiflow.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.myouo.lexiflow.android.viewmodel.VocabularyViewModel

@Composable
fun VocabularyScreen(viewModel: VocabularyViewModel) {
    val state = viewModel.state.collectAsState().value
    
    var customUrl by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text("词库管理", style = MaterialTheme.typography.headlineMedium)
        }
        
        // Section 1/2: Catalog List & Progress
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("官方词库", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (state.isImporting) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(state.importStatus, modifier = Modifier.padding(top = 8.dp))
                    } else if (!state.catalog?.datasets.isNullOrEmpty()) {
                        state.catalog!!.datasets.forEach { dataset ->
                            ListItem(
                                headlineContent = { Text(dataset.name) },
                                supportingContent = { Text("词库源文件") },
                                trailingContent = {
                                    Button(onClick = { viewModel.importDataset(dataset) }) {
                                        Text("导入")
                                    }
                                }
                            )
                        }
                    } else {
                        Button(onClick = { viewModel.fetchCatalog("https://raw.githubusercontent.com/dummy/catalog.json") }) {
                            Text("刷新词库列表")
                        }
                    }
                    
                    if (!state.error.isNullOrEmpty()) {
                        Text(state.error, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        
        // Section 3: Custom Source
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("自定义词库", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customUrl,
                        onValueChange = { customUrl = it },
                        label = { Text("https://raw.githubusercontent.com/.../dataset.json") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.fetchCatalog(customUrl) }, 
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("导入自定义词库")
                    }
                }
            }
        }

        // Section 4: Installed Datasets
        item {
            Text("已安装词库", style = MaterialTheme.typography.titleMedium)
            // Hardcoded for UI mapping
            ListItem(
                headlineContent = { Text("English Top 5000") },
                supportingContent = { Text("5000 词") },
                trailingContent = {
                    TextButton(onClick = {}, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Text("删除")
                    }
                }
            )
        }
    }
}
