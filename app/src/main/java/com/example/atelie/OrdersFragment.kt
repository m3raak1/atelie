package com.example.atelie

import android.content.Context
import android.content.Intent
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
import com.example.atelie.repositories.OrderRepository
import com.example.atelie.views.ContainerCardView
import com.example.atelie.views.OrderCardView
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

    private lateinit var cardContainer: ContainerCardView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardContainer = view.findViewById<ContainerCardView>(R.id.card_container)

        val addOrderBtn = view.findViewById<ImageButton>(R.id.add_order)

        addOrderBtn.setOnClickListener {
                startActivity(Intent(requireContext(), NewOrderActivity::class.java))
        }

        initializeOrderList(view)
    }

    fun initializeOrderList(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            val ordersWithClients = OrderRepository.getOrdersWithClientsAndClothingCount()

            for ((_, order) in ordersWithClients.withIndex()) {
                cardContainer.addCard(loadOrderCard(view.context, order))
            }
        }
    }

    fun loadOrderCard(context: Context, order: OrderWithClientAndItemClothingCount) =
        OrderCardView(context).apply {
            bind(order)
        }
}