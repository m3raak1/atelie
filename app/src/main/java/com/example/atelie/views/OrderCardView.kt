package com.example.atelie.views

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.atelie.OrderActivity
import com.example.atelie.OrderWithClientAndItemClothingCount
import com.example.atelie.R

class OrderCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.item_order, this, true)
    }

    fun bind(orderWithClient: OrderWithClientAndItemClothingCount) {
        findViewById<TextView>(R.id.name_client).text = buildString {
            append(orderWithClient.clients.name)
            append(" · ")
            append(orderWithClient.clients.phone)
        }

        findViewById<TextView>(R.id.order_info).text = buildString {
            append("Posição ")
            append(orderWithClient.position)
            append(" · ")
            append(orderWithClient.items_clothing.firstOrNull()?.count ?: 0)
            append(" Peça")
        }

        findViewById<TextView>(R.id.order_price).text = buildString {
            append("R$ ")
            append(orderWithClient.price)
        }

        findViewById<TextView>(R.id.order_date).text = buildString {
            append("Entrega: ")
            append(orderWithClient.date_exit.dayOfMonth)
            append("/")
            append("%02d".format(orderWithClient.date_exit.monthNumber))
            append("/")
            append(orderWithClient.date_exit.year)
        }

        setOnClickListener {
            val intent = Intent(context, OrderActivity::class.java)
            intent.putExtra("orderId", orderWithClient.id)
            intent.putExtra("clientId", orderWithClient.clients.id)
            context.startActivity(intent)
        }
    }
}
