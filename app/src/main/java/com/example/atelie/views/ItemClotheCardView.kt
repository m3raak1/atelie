package com.example.atelie.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.atelie.ItemClothingFull
import com.example.atelie.R

class ItemClotheCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.item_clothe_card, this, true)
    }

    fun bind(itemClothe: ItemClothingFull) {
        findViewById<TextView>(R.id.title).text = itemClothe.clothing_type.name.replaceFirstChar { it.uppercaseChar() }
        findViewById<TextView>(R.id.desc).text = itemClothe.desc

        val servicesString = itemClothe.services.joinToString(" - ") {
            it.name.replaceFirstChar { c -> c.uppercaseChar() }
        }

        findViewById<TextView>(R.id.services).text = servicesString
        findViewById<TextView>(R.id.price).text = "${itemClothe.price} R$"
        findViewById<TextView>(R.id.quantity).text = "1x"
    }
}