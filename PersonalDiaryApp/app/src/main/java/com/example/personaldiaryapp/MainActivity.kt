package com.example.personaldiaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.personaldiaryapp.ui.theme.PersonalDiaryAppTheme
import com.example.personaldiaryapp.viewmodel.DiaryViewModel
import android.content.Context
import android.app.Application
import androidx.activity.compose.setContent
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button

import androidx.compose.runtime.remember

import androidx.compose.ui.unit.dp
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.TextUnit
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


class MainActivity : ComponentActivity() {
    private val viewModel: DiaryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(viewModel)
        }
    }
}

@Composable
fun MainScreen(viewModel: DiaryViewModel) {
    val fontSize by viewModel.fontSizeState.collectAsState()
    val isDarkTheme by viewModel.themeState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        if (showSettings) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { showSettings = false }
            )
        } else {
            Scaffold(
                topBar = {
                    DiaryAppBar(
                        viewModel = viewModel,
                        onSettingsClick = { showSettings = true }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { viewModel.saveEntry() }) {
                        Icon(Icons.Default.PlayArrow, "Save Entry")
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    CalendarView(viewModel)
                    Spacer(Modifier.height(16.dp))
                    DiaryEditor(viewModel, fontSize)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarView(viewModel: DiaryViewModel) {
    val showDatePicker = remember { mutableStateOf(false) }
    val selectedDate by viewModel.currentDate.collectAsState()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.timeInMillis
    )

    Button(onClick = { showDatePicker.value = true }) {
        Text("Select Date")
    }

    if (showDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = it
                            }
                            viewModel.setDate(calendar)
                        }
                        showDatePicker.value = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker.value = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun DiaryEditor(viewModel: DiaryViewModel, fontSize: Float) {
    val text by viewModel.entryContent.collectAsState()

    BasicTextField(
        value = text,
        onValueChange = { newText ->
            viewModel.updateContent(newText)
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        textStyle = LocalTextStyle.current.copy(
            fontSize = TextUnit(fontSize, TextUnitType.Sp)
        ),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.padding(8.dp)) {
                if (text.isEmpty()) {
                    Text(
                        "Write your diary entry...",
                        style = LocalTextStyle.current.copy(
                            fontSize = TextUnit(fontSize, TextUnitType.Sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryAppBar(
    viewModel: DiaryViewModel,
    onSettingsClick: () -> Unit
) {
    val isDarkTheme by viewModel.themeState.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }

    TopAppBar(
        title = { Text("Diary Entry for ${dateFormat.format(currentDate.time)}") },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, "Settings")
            }
            IconButton(onClick = { viewModel.toggleTheme() }) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Toggle Theme"
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DiaryViewModel,
    onBack: () -> Unit
) {
    val fontSize by viewModel.fontSizeState.collectAsState()
    val isDarkTheme by viewModel.themeState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dark Theme")
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.toggleTheme() }
                )
            }

            Spacer(Modifier.height(24.dp))

            Text("Font Size: ${"%.1f".format(fontSize)}sp")
            Slider(
                value = fontSize,
                onValueChange = { viewModel.updateFontSize(it) },
                valueRange = 12f..24f,
                steps = 12,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


