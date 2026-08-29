package com.ai.tasktodo

data class Task(
    val id: Long,
    val title: String,
    val done: Boolean = false,
    val category: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
