package com.example.todoapp.data.repository

import com.example.todoapp.data.local.Task
import com.example.todoapp.data.local.TaskDao
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    suspend fun addTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    fun getAllTasks(): Flow<List<Task>>
    fun getCompletedTasks(): Flow<List<Task>>
    fun getPendingTasks(): Flow<List<Task>>
}

class TaskRepositoryImpl(private val taskDao: TaskDao) : TaskRepository {
    override suspend fun addTask(task: Task) = taskDao.insert(task)
    override suspend fun updateTask(task: Task) = taskDao.update(task)
    override suspend fun deleteTask(task: Task) = taskDao.delete(task)
    override fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()
    override fun getCompletedTasks(): Flow<List<Task>> = taskDao.getCompletedTasks()
    override fun getPendingTasks(): Flow<List<Task>> = taskDao.getPendingTasks()
}