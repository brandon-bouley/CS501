package com.example.personaldiaryapp.data.local

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class FileStorageManager(private val context: Context) {
    private val entriesDir by lazy {
        File(context.filesDir, "diary_entries").apply { mkdirs() }
    }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun saveEntry(date: Date, content: String) {
        val dateString = dateFormat.format(date)
        File(entriesDir, "$dateString.txt").writeText(content)
    }

    fun loadEntry(date: Date): String? {
        val dateString = dateFormat.format(date)
        val file = File(entriesDir, "$dateString.txt")
        return if (file.exists()) file.readText() else null
    }

    fun listEntries(): List<Date> {
        return entriesDir.listFiles()?.mapNotNull { file ->
            try {
                dateFormat.parse(file.nameWithoutExtension)
            } catch (e: ParseException) {
                null
            }
        } ?: emptyList()
    }

    fun deleteEntry(date: Date) {
        val dateString = dateFormat.format(date)
        File(entriesDir, "$dateString.txt").delete()
    }
}