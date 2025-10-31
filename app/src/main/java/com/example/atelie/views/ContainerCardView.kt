package com.example.atelie.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.atelie.R
import com.google.android.material.card.MaterialCardView
import androidx.core.content.withStyledAttributes

class ContainerCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : MaterialCardView(context, attrs) {

    private var placeholderText: String? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_card_container, this, true)

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.ContainerCardView, 0, 0) {
                placeholderText = getString(R.styleable.ContainerCardView_placeholderText)
            }
        }

        findViewById<TextView>(R.id.placeholder).setText(placeholderText ?: "Nenhum foi card adicionado")
    }

    private val container = findViewById<LinearLayout>(R.id.container_card)
    private val placeholder = findViewById<TextView>(R.id.placeholder)

    fun addCard(card: View) {

        placeholder.visibility = GONE

        if (container.childCount > 1) {
            val divider = LayoutInflater.from(context).inflate(R.layout.view_divider, this, false)
            container.addView(divider)
        }

        container.addView(card)
    }

    fun deleteCard(card: View) {
        val index = container.indexOfChild(card)
        if (index > 1) {
            val divider = container.getChildAt(index - 1)
            container.removeView(divider)
        }

        container.removeView(card)

        if (container.childCount == 1) {
            findViewById<TextView>(R.id.item_clothe_placeholder).visibility = VISIBLE
            onEmpty?.invoke()
        }
    }

    fun deleteAllCards() {
        if (container.childCount <= 1) return
        container.removeViews(1, container.childCount - 1)
    }

    private var onEmpty: (() -> Unit)? = null

    fun setOnEmptyListener(listener: () -> Unit){
        onEmpty = listener
    }
}