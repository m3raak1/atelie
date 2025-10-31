package com.example.atelie.repositories

import android.util.Log
import com.example.atelie.OrderWithClientAndItemClothingCount
import com.example.atelie.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

object ItemClothingRepository {
    suspend fun getItemClothingWithServiceAndClothType(): List<OrderWithClientAndItemClothingCount> {
        return try {
            val columns = Columns.raw("""
                *,
                types_clothing(*),
                item_clothing_service(
                    service(*)
                )
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