package com.ai.tasktodo

import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ai.tasktodo.databinding.ItemTaskBinding

class TaskAdapter(
    private val onToggle: (Task) -> Unit,
    private val onDelete: (Task) -> Unit,
    private val onEdit: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val items = mutableListOf<Task>()
    private val animatedIds = mutableSetOf<Long>()

    init {
        setHasStableIds(true)
    }

    fun submit(list: List<Task>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = items[position].id

    inner class TaskViewHolder(val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            val context = binding.root.context
            val category = Categories.of(task.category)

            binding.textTitle.text = task.title
            binding.textTitle.paintFlags = if (task.done) {
                binding.textTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                binding.textTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            binding.textTitle.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (task.done) R.color.text_disabled else R.color.text_primary
                )
            )

            binding.dotCategory.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ContextCompat.getColor(context, category.colorRes))
            }
            binding.textCategory.text = context.getString(category.nameRes)

            binding.checkButton.setBackgroundResource(
                if (task.done) R.drawable.bg_circle_checked else R.drawable.bg_circle_unchecked
            )
            binding.checkIcon.visibility = if (task.done) ImageView.VISIBLE else ImageView.INVISIBLE

            binding.checkButton.setOnClickListener { onToggle(task) }
            binding.btnDelete.setOnClickListener { onDelete(task) }
            binding.root.setOnClickListener { onEdit(task) }

            if (!animatedIds.contains(task.id)) {
                animatedIds.add(task.id)
                binding.root.alpha = 0f
                binding.root.scaleX = 0.94f
                binding.root.scaleY = 0.94f
                binding.root.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(280)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        }
    }
}
