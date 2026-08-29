package com.ai.tasktodo

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class TaskRepository(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences("tasktodo_prefs", Context.MODE_PRIVATE)

    fun load(): List<Task> {
        val raw = prefs.getString(KEY_TASKS, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { i -> array.getJSONObject(i).toTask() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun save(tasks: List<Task>) {
        val array = JSONArray()
        tasks.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_TASKS, array.toString()).apply()
    }

    private fun Task.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("done", done)
        put("category", category)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toTask(): Task = Task(
        id = optLong("id", System.currentTimeMillis()),
        title = optString("title", ""),
        done = optBoolean("done", false),
        category = optInt("category", 0),
        createdAt = optLong("createdAt", 0L)
    )

    private companion object {
        const val KEY_TASKS = "tasks"
    }
}
