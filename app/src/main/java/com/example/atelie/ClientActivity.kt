package com.example.atelie

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.atelie.repositories.ClientRepository
import com.example.atelie.views.OrderCardView
import com.google.android.material.button.MaterialButton
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi

class ClientActivity : AppCompatActivity() {
    var client: Client? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_client)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.back_btn).setOnClickListener {
            finish()
        }

        val clientId = intent.getIntExtra("clientId", -1)

        lifecycleScope.launch {
            client = ClientRepository.getClientById(clientId)

            if (client != null) {
                initializeInfo()
                initializeOrderList()
            } else {
                finish()
            }
        }

        findViewById<MaterialButton>(R.id.add_order).setOnClickListener {
            val intent = Intent(this, NewOrderActivity::class.java)
            intent.putExtra("clientString", "${client!!.name} - ${client!!.phone}")
            startActivity(intent)
        }
    }

    fun initializeInfo() {
        findViewById<TextView>(R.id.title).setText(client!!.name.replaceFirstChar { it.uppercase() })
        findViewById<TextView>(R.id.phone).setText(formatPhoneNumber(client!!.phone))
    }

    fun initializeOrderList(onError: ((Throwable) -> Unit)? = null) {
        lifecycleScope.launch {
            try {
                val columns = Columns.raw("""
                    *,
                    clients(*),
                    items_clothing(count)
                """.trimIndent())

                val ordersWithClients = supabase
                    .from("orders")
                    .select(columns = columns) {
                        filter {
                            eq("id_client", client!!.id)
                        }
                    }.decodeList<OrderWithClientAndItemClothingCount>()

                if (ordersWithClients.isEmpty()) {
                    findViewById<TextView>(R.id.order_placeholder).visibility = View.VISIBLE
                    return@launch
                }

                val container = findViewById<LinearLayout>(R.id.order_list)

                for ((index, order) in ordersWithClients.withIndex()) {
                    addOrderCard(order)

                    if (index < ordersWithClients.lastIndex) {
                        val divider = layoutInflater.inflate(R.layout.view_divider, container, false)
                        container.addView(divider)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("Supabase", "Erro ao carregar entradas")
                onError?.invoke(e)
            }
        }
    }

    fun addOrderCard(orderWithClient: OrderWithClientAndItemClothingCount) {
        val container = findViewById<LinearLayout>(R.id.order_list)

        val newOrderCard = OrderCardView(this)
        newOrderCard.bind(orderWithClient)

        container.addView(newOrderCard)
    }
}