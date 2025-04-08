package com.example.personaldiaryapp.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaldiaryapp.data.local.FileStorageManager
import com.example.personaldiaryapp.data.local.PreferencesDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class DiaryViewModel(application: Application) : AndroidViewModel(application) {
    // Initialize dependencies
    private val prefs = PreferencesDataStore(application)
    private val storage = FileStorageManager(application)

    // Theme state
    private val _themeState = MutableStateFlow(false)
    val themeState: StateFlow<Boolean> = _themeState.asStateFlow()

    // Font size state
    private val _fontSizeState = MutableStateFlow(16f)
    val fontSizeState: StateFlow<Float> = _fontSizeState.asStateFlow()

    // Date state
    private val _currentDate = MutableStateFlow(Calendar.getInstance())
    val currentDate: StateFlow<Calendar> = _currentDate.asStateFlow()

    // Entry content
    private val _entryContent = MutableStateFlow("")
    val entryContent: StateFlow<String> = _entryContent.asStateFlow()

    init {
        viewModelScope.launch {
            // Load initial preferences
            prefs.themeFlow.collect { isDarkTheme ->
                _themeState.value = isDarkTheme
            }
            prefs.fontSizeFlow.collect { fontSize ->
                _fontSizeState.value = fontSize
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val newTheme = !_themeState.value
            _themeState.value = newTheme
            prefs.toggleTheme(newTheme)
        }
    }

    fun updateFontSize(size: Float) {
        viewModelScope.launch {
            _fontSizeState.value = size
            prefs.updateFontSize(size)
        }
    }

    fun setDate(calendar: Calendar) {
        _currentDate.value = calendar
    }

    fun updateContent(content: String) {
        _entryContent.value = content
    }

    fun saveEntry() {
        viewModelScope.launch {
            storage.saveEntry(_currentDate.value.time, _entryContent.value)
        }
    }
}