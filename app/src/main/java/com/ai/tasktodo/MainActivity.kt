package com.ai.tasktodo

import android.animation.ValueAnimator
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.ai.tasktodo.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TaskFilter {
    ALL, ACTIVE, DONE
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: TaskRepository
    private lateinit var adapter: TaskAdapter

    private val tasks = mutableListOf<Task>()
    private var filter = TaskFilter.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = TaskRepository(this)
        tasks.addAll(repository.load().sortedWith(taskOrder))

        adapter = TaskAdapter(
            onToggle = { task -> toggleTask(task) },
            onDelete = { task -> deleteTask(task) },
            onEdit = { task -> openEditor(task) }
        )
        binding.recyclerTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerTasks.adapter = adapter

        setupInsets()
        setupHeader()
        setupTabs()
        binding.btnAdd.setOnClickListener { openEditor(null) }
        refresh()
    }

    private val taskOrder = compareBy<Task> { it.done }.thenByDescending { it.createdAt }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            binding.content.updatePadding(
                top = statusBar + dp(4),
                bottom = navBar + dp(8)
            )
            val lp = binding.btnAdd.layoutParams as FrameLayout.LayoutParams
            lp.bottomMargin = navBar + dp(24)
            binding.btnAdd.layoutParams = lp
            insets
        }
    }

    private fun setupHeader() {
        val locale = Locale.forLanguageTag("es-ES")
        val formatter = SimpleDateFormat("EEEE, d 'de' MMMM", locale)
        val date = formatter.format(Date())
        binding.textDate.text = date.replaceFirstChar { it.titlecase(locale) }
    }

    private fun setupTabs() {
        binding.tabAll.setOnClickListener { setFilter(TaskFilter.ALL) }
        binding.tabActive.setOnClickListener { setFilter(TaskFilter.ACTIVE) }
        binding.tabDone.setOnClickListener { setFilter(TaskFilter.DONE) }
    }

    private fun setFilter(newFilter: TaskFilter) {
        if (filter == newFilter) return
        filter = newFilter
        updateTabs()
        refresh()
    }

    private fun updateTabs() {
        val tabs = listOf(binding.tabAll, binding.tabActive, binding.tabDone)
        tabs.forEachIndexed { index, tab ->
            val selected = index == filter.ordinal
            tab.background = if (selected) {
                ContextCompat.getDrawable(this, R.drawable.bg_tab_selected)
            } else {
                null
            }
            tab.setTextColor(
                ContextCompat.getColor(this, if (selected) R.color.white else R.color.text_secondary)
            )
        }
    }

    private fun openEditor(existing: Task?) {
        AddTaskDialog.show(this, existing) { title, category ->
            if (existing == null) {
                tasks.add(Task(id = System.currentTimeMillis(), title = title, category = category))
            } else {
                val index = tasks.indexOfFirst { it.id == existing.id }
                if (index >= 0) tasks[index] = tasks[index].copy(title = title, category = category)
            }
            tasks.sortWith(taskOrder)
            repository.save(tasks)
            refresh()
        }
    }

    private fun toggleTask(task: Task) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index < 0) return
        tasks[index] = tasks[index].copy(done = !tasks[index].done)
        tasks.sortWith(taskOrder)
        repository.save(tasks)
        refresh()
    }

    private fun deleteTask(task: Task) {
        tasks.removeAll { it.id == task.id }
        repository.save(tasks)
        refresh()

        val snackbar = Snackbar.make(binding.root, R.string.task_deleted, Snackbar.LENGTH_LONG)
            .setAction(R.string.undo) {
                tasks.add(task)
                tasks.sortWith(taskOrder)
                repository.save(tasks)
                refresh()
            }
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.accent_light))
        snackbar.view.background = ContextCompat.getDrawable(this, R.drawable.bg_snackbar)
        snackbar.view.elevation = 12f
        snackbar.show()
    }

    private fun refresh() {
        val visible = when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.ACTIVE -> tasks.filter { !it.done }
            TaskFilter.DONE -> tasks.filter { it.done }
        }
        adapter.submit(visible)

        val total = tasks.size
        val doneCount = tasks.count { it.done }
        val percent = if (total == 0) 0 else doneCount * 100 / total

        binding.textCount.text = visible.size.toString()
        animateProgress(percent)
        binding.textPercent.text = "$percent%"
        binding.textSummary.text = when {
            total == 0 -> getString(R.string.summary_empty)
            doneCount == total -> getString(R.string.summary_all_done)
            else -> getString(R.string.summary_partial, doneCount, total)
        }

        val isEmpty = visible.isEmpty()
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        if (isEmpty) {
            binding.textEmptyTitle.setText(
                when (filter) {
                    TaskFilter.ALL -> R.string.empty_all_title
                    TaskFilter.ACTIVE -> R.string.empty_active_title
                    TaskFilter.DONE -> R.string.empty_done_title
                }
            )
            binding.textEmptyBody.setText(
                when (filter) {
                    TaskFilter.ALL -> R.string.empty_all_body
                    TaskFilter.ACTIVE -> R.string.empty_active_body
                    TaskFilter.DONE -> R.string.empty_done_body
                }
            )
        }
        updateTabs()
    }

    private fun animateProgress(target: Int) {
        val current = binding.progressSummary.progress
        if (current == target) return
        ValueAnimator.ofInt(current, target).apply {
            duration = 500
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                binding.progressSummary.progress = it.animatedValue as Int
            }
            start()
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
