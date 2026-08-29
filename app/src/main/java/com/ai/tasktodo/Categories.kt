package com.ai.tasktodo

import androidx.annotation.ColorRes
import androidx.annotation.StringRes

object Categories {

    data class Category(
        @param:StringRes val nameRes: Int,
        @param:ColorRes val colorRes: Int
    )

    val ALL: List<Category> = listOf(
        Category(R.string.cat_personal, R.color.cat_personal),
        Category(R.string.cat_trabajo, R.color.cat_trabajo),
        Category(R.string.cat_estudio, R.color.cat_estudio),
        Category(R.string.cat_salud, R.color.cat_salud),
        Category(R.string.cat_otro, R.color.cat_otro)
    )

    fun of(index: Int): Category = ALL[if (index in ALL.indices) index else 0]
}
