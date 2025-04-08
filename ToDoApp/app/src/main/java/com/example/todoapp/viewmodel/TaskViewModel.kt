package com.example.todoapp.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.local.Task
import com.example.todoapp.data.repository.TaskRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TaskFilter { ALL, COMPLETED, PENDING }

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    private val _showDialog = mutableStateOf(false)
    val showDialog: State<Boolean> = _showDialog

    private val _selectedTask = mutableStateOf<Task?>(null)
    val selectedTask: State<Task?> = _selectedTask

    private val _currentFilter = MutableStateFlow(TaskFilter.ALL)
    val currentFilter: StateFlow<TaskFilter> = _currentFilter.asStateFlow()

    private val tasksFlow = _currentFilter.flatMapLatest { filter ->
        when (filter) {
            TaskFilter.ALL -> repository.getAllTasks()
            TaskFilter.COMPLETED -> repository.getCompletedTasks()
            TaskFilter.PENDING -> repository.getPendingTasks()
        }
    }

    val tasks: StateFlow<List<Task>> = tasksFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun showAddDialog() { _showDialog.value = true }
    fun dismissDialog() { _showDialog.value = false }

    fun setSelectedTask(task: Task?) { _selectedTask.value = task }

    fun setFilter(filter: TaskFilter) {
        _currentFilter.value = filter
    }

    fun addTask(title: String, description: String?) = viewModelScope.launch {
        repository.addTask(Task(title = title, description = description))
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        repository.updateTask(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
    }
}

class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TaskViewModel(repository) as T
    }
}