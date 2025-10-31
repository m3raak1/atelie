package com.example.atelie.views

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.atelie.Client
import com.example.atelie.ClientActivity
import com.example.atelie.R
import com.example.atelie.formatPhoneNumber

class ClientCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.item_client, this, true)
    }

    fun bind(client: Client) {
        findViewById<TextView>(R.id.name_client).text = client.name.replaceFirstChar { it.uppercaseChar() }
        findViewById<TextView>(R.id.phone_client).text = formatPhoneNumber(client.phone)

        setOnClickListener {
            val intent = Intent(context, ClientActivity::class.java)
            intent.putExtra("clientId", client.id)
            context.startActivity(intent)
        }
    }
}