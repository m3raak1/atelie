package com.example.atelie

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch
import kotlinx.datetime.format
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class OrdersFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_orders, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addOrderBtn = view.findViewById<ImageButton>(R.id.add_order)

        addOrderBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NewOrderFragment())
                .addToBackStack(null)
                .commit()
        }

        initializeOrderList(view) { erro ->
            Toast.makeText(requireContext(), "Erro ao carregar entradas", Toast.LENGTH_SHORT).show()
        }
    }

    fun initializeOrderList(view: View, onError: ((Throwable) -> Unit)? = null) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val columns = Columns.raw("""
                    *,
                    clients(*),
                    items_clothing(count)
                """.trimIndent())

                val ordersWithClients = supabase
                    .from("orders")
                    .select(columns = columns)
                    .decodeList<OrderWithClientAndItemClothingCount>()

                val container = view.findViewById<LinearLayout>(R.id.order_list)

                for ((index, order) in ordersWithClients.withIndex()) {
                    addOrderCard(view, order)

                    if (index < ordersWithClients.lastIndex) {
                        val divider = layoutInflater.inflate(R.layout.view_divider, container, false)
                        container.addView(divider)
                    }
                }

                Log.d("Supabase", ordersWithClients.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("Supabase", "Erro ao carregar entradas")
                onError?.invoke(e)
            }
        }
    }

    fun addOrderCard(view: View, orderWithClient: OrderWithClientAndItemClothingCount) {
        val container = view.findViewById<LinearLayout>(R.id.order_list)

        val newOrderCard = layoutInflater.inflate(R.layout.item_order, container, false)

        newOrderCard.findViewById<TextView>(R.id.name_client).text = buildString {
            append(orderWithClient.clients.name)
            append(" · ")
            append(orderWithClient.clients.phone)
        }

        newOrderCard.findViewById<TextView>(R.id.order_info).text = buildString {
            append("Posição ")
            append(orderWithClient.position)
            append(" · ")
            append(orderWithClient.items_clothing.firstOrNull()?.count ?: 0)
            append(" Peça")
        }

        newOrderCard.findViewById<TextView>(R.id.order_price).text = buildString {
            append("R$ ")
            append(orderWithClient.price)
        }

        newOrderCard.findViewById<TextView>(R.id.order_date).text = buildString {
            append("Entrega: ")
            append(orderWithClient.date_exit.dayOfMonth)
            append("/")
            append("%02d".format(orderWithClient.date_exit.monthNumber))
            append("/")
            append(orderWithClient.date_exit.year)
        }

        container.addView(newOrderCard)
    }
}