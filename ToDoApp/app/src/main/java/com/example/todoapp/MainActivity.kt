package com.example.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.todoapp.ui.theme.ToDoAppTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.todoapp.data.local.AppDatabase
import com.example.todoapp.data.local.Task
import com.example.todoapp.data.repository.TaskRepositoryImpl
import com.example.todoapp.viewmodel.TaskFilter
import com.example.todoapp.viewmodel.TaskViewModel
import com.example.todoapp.viewmodel.TaskViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<TaskViewModel> {
        TaskViewModelFactory(
            TaskRepositoryImpl(
                AppDatabase.getDatabase(this).taskDao()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToDoAppTheme {
                Surface {
                    TaskListScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun TaskListScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()

    Scaffold(
        topBar = {
            Box(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                FilterBar(viewModel)
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onCheckedChange = { completed ->
                            viewModel.updateTask(task.copy(isCompleted = completed))
                        },
                        onEdit = {
                            viewModel.setSelectedTask(task)
                            viewModel.showAddDialog()
                        },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }

            if (viewModel.showDialog.value) {
                TaskDialog(
                    task = viewModel.selectedTask.value,
                    onDismiss = { viewModel.dismissDialog() },
                    onConfirm = { title, desc ->
                        viewModel.selectedTask.value?.let { existingTask ->
                            viewModel.updateTask(existingTask.copy(
                                title = title,
                                description = desc
                            ))
                        } ?: viewModel.addTask(title, desc)
                    }
                )
            }
        }
    )
}

@Composable
private fun TaskList(tasks: List<Task>, viewModel: TaskViewModel) {
    LazyColumn {
        items(tasks) { task ->
            TaskItem(
                task = task,
                onCheckedChange = { completed ->
                    viewModel.updateTask(task.copy(isCompleted = completed))
                },
                onEdit = {
                    viewModel.setSelectedTask(task)
                    viewModel.showAddDialog()
                },
                onDelete = { viewModel.deleteTask(task) }
            )
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Checkbox(
            checked = task.isCompleted,
            onCheckedChange = onCheckedChange
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge
            )
            task.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

@Composable
private fun FilterBar(viewModel: TaskViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        TaskFilter.entries.forEach { filter ->
            FilterChip(
                selected = viewModel.currentFilter.value == filter,
                onClick = { viewModel.setFilter(filter) },
                label = { Text(filter.name) }
            )
        }
    }
}

@Composable
fun TaskDialog(
    task: Task?,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (task == null) "New Task" else "Edit Task") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(title, description.ifBlank { null })
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

