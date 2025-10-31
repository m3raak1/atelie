package com.example.atelie

import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.atelie.repositories.ClientRepository
import com.example.atelie.repositories.OrderRepository
import kotlinx.coroutines.launch

class OrderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_order)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        lifecycleScope.launch {
            val orderId = intent.getIntExtra("orderId", -1).takeIf { it != -1 }
                ?: return@launch finish()

            val order = OrderRepository.getOrderById(orderId) ?: return@launch finish()
            Log.d("Test", order.id_client.toString())
            val client = ClientRepository.getClientById(order.id_client) ?: return@launch finish()

            setTitleData(order)
            setInfoData(order)
            setClientData(client)
            setStatusData(order)
        }

    }

    private fun setClientData(client: Client) {
        findViewById<TextView>(R.id.client_name).setText(client.name)
        findViewById<TextView>(R.id.phone_number).setText(formatPhoneNumber(client.phone))
    }

    private fun setTitleData(order: Order) {
        findViewById<TextView>(R.id.title).setText("Entrada #${order.id}")
        findViewById<TextView>(R.id.subtitle).setText("Número ${order.position}")
    }

    private fun setInfoData(order: Order) {
        findViewById<TextView>(R.id.entry_date).setText(toStringDate(order.created_at))
        findViewById<TextView>(R.id.delivery_date).setText(toStringDate(order.date_exit))
    }

    private fun setStatusData(order: Order) {
        val statusView = findViewById<AutoCompleteTextView>(R.id.input_exit)
        val optionsArray = resources.getStringArray(R.array.status_items)

        val optionIndex = if (order.exited_at != null) 1 else 0

        statusView.setText(optionsArray[optionIndex], false)
    }
}