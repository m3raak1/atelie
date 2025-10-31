package com.example.atelie.repositories

import android.util.Log
import com.example.atelie.Order
import com.example.atelie.OrderWithClientAndItemClothingCount
import com.example.atelie.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

object OrderRepository {
    suspend fun getOrderById(id: Int): Order? {
        return try {
            supabase.from("orders")
                .select() {
                    filter {
                        eq("id", id)
                    }
                }.decodeSingle<Order>()
        } catch (e: Exception) {
            Log.e("Supabase", "Erro ao buscar cliente", e)
            null
        }
    }

    suspend fun getOrdersWithClientsAndClothingCount(): List<OrderWithClientAndItemClothingCount> {
        return try {
            val columns = Columns.raw("""
            *,
            clients(*),
            items_clothing(count)
        """.trimIndent())

            supabase.from("orders")
                .select(columns = columns)
                .decodeList<OrderWithClientAndItemClothingCount>()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("Supabase", "Erro ao carregar entradas")
            emptyList()
        }
    }
}