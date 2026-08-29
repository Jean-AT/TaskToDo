package com.ai.tasktodo

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.ai.tasktodo.databinding.DialogAddTaskBinding

object AddTaskDialog {

    fun show(context: Context, existing: Task?, onSave: (title: String, category: Int) -> Unit) {
        val binding = DialogAddTaskBinding.inflate(LayoutInflater.from(context))
        var selected = existing?.category ?: 0

        val chips = mutableListOf<LinearLayout>()
        Categories.ALL.forEachIndexed { index, category ->
            val chip = createChip(context, category, selected == index) { position ->
                selected = position
                chips.forEachIndexed { i, c ->
                    setChipSelected(context, c, Categories.ALL[i], position == i)
                }
            }
            chips.add(chip)
            binding.chipsRow.addView(chip)
        }

        if (existing != null) {
            binding.textDialogTitle.setText(R.string.edit_task)
            binding.btnSave.setText(R.string.save)
            binding.inputTitle.setText(existing.title)
            binding.inputTitle.setSelection(existing.title.length)
        }

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(binding.root)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
            val width = (context.resources.displayMetrics.widthPixels * 0.9f).toInt()
            setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }

        binding.btnCancel.setOnClickListener { dialog.dismiss() }
        binding.btnSave.setOnClickListener {
            val title = binding.inputTitle.text?.toString()?.trim()
            if (title.isNullOrEmpty()) {
                binding.inputTitle.error = context.getString(R.string.error_empty)
                return@setOnClickListener
            }
            onSave(title, selected)
            dialog.dismiss()
        }

        dialog.setOnShowListener {
            binding.inputTitle.requestFocus()
            binding.inputTitle.post {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.inputTitle, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        dialog.show()
    }

    private fun createChip(
        context: Context,
        category: Categories.Category,
        selected: Boolean,
        onClick: (Int) -> Unit
    ): LinearLayout {
        val chip = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(context, 14), dp(context, 8), dp(context, 14), dp(context, 8))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp(context, 8) }
            isClickable = true
            isFocusable = true
            outlineProvider = ViewOutlineProvider.BACKGROUND
        }

        val dot = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(dp(context, 8), dp(context, 8))
                .apply { marginEnd = dp(context, 6) }
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ContextCompat.getColor(context, category.colorRes))
            }
        }

        val label = TextView(context).apply {
            text = context.getString(category.nameRes)
            textSize = 12f
        }

        chip.addView(dot)
        chip.addView(label)
        chip.setOnClickListener { onClick(Categories.ALL.indexOf(category)) }
        setChipSelected(context, chip, category, selected)
        return chip
    }

    private fun setChipSelected(
        context: Context,
        chip: LinearLayout,
        category: Categories.Category,
        selected: Boolean
    ) {
        val color = ContextCompat.getColor(context, category.colorRes)
        val drawable = GradientDrawable().apply {
            cornerRadius = dp(context, 18).toFloat()
            if (selected) {
                setColor((color and 0x00FFFFFF) or 0x26000000)
                setStroke(dp(context, 1), color)
            } else {
                setColor(0x14FFFFFF)
                setStroke(dp(context, 1), 0x33FFFFFF)
            }
        }
        chip.background = drawable
        val label = chip.getChildAt(1) as TextView
        label.setTextColor(
            if (selected) Color.WHITE
            else ContextCompat.getColor(context, R.color.text_secondary)
        )
    }

    private fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
