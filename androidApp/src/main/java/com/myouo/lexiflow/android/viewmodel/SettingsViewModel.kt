package com.myouo.lexiflow.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SettingsState(
    val dailyTarget: String = "200",
    val maxSenses: String = "3",
    val recallThreshold: String = "0.85",
    val isRandomOrder: Boolean = false,
    val theme: String = "系统默认",
    val language: String = "简体中文"
)

class SettingsViewModel : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state

    fun updateDailyTarget(value: String) {
        if (value.isEmpty() || value.toIntOrNull() != null) {
            _state.value = _state.value.copy(dailyTarget = value)
            persistSettings()
        }
    }

    fun updateMaxSenses(value: String) {
        if (value.isEmpty() || value.toIntOrNull() != null) {
            _state.value = _state.value.copy(maxSenses = value)
            persistSettings()
        }
    }

    fun updateRecallThreshold(value: String) {
        // Allow decimals during typing, validate on save
        _state.value = _state.value.copy(recallThreshold = value)
        persistSettings()
    }

    fun toggleRandomOrder(checked: Boolean) {
        _state.value = _state.value.copy(isRandomOrder = checked)
        persistSettings()
    }

    fun exportBackup() {
        // BackupManager trigger here
    }

    fun importBackup() {
        // BackupManager trigger here
    }

    private fun persistSettings() {
        // Mock DB persist logic
        // db.insertSetting("dailyTarget", _state.value.dailyTarget)
    }
}
