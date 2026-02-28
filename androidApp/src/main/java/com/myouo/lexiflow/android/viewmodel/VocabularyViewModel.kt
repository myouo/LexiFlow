package com.myouo.lexiflow.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myouo.lexiflow.domain.VocabularyImporter
import com.myouo.lexiflow.network.models.CatalogDto
import com.myouo.lexiflow.network.models.DatasetDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ImportState(
    val catalog: CatalogDto? = null,
    val isImporting: Boolean = false,
    val importProgress: Float = 0f,
    val importStatus: String = "",
    val error: String? = null
)

class VocabularyViewModel(private val importer: VocabularyImporter) : ViewModel() {
    private val _state = MutableStateFlow(ImportState())
    val state: StateFlow<ImportState> = _state
    
    fun fetchCatalog(url: String) {
        viewModelScope.launch {
            try {
                val catalog = importer.fetchCatalog(url)
                _state.value = _state.value.copy(catalog = catalog, error = null)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
    
    fun importDataset(dataset: DatasetDto) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isImporting = true, importStatus = "Downloading \${dataset.name}...")
            try {
                importer.importDataset(dataset.url, dataset.sha256)
                _state.value = _state.value.copy(isImporting = false, importStatus = "Import Complete!", error = null)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isImporting = false, importStatus = "Import Failed", error = e.message)
            }
        }
    }
}
