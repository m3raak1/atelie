package com.example.atelie.repositories

import android.util.Log
import com.example.atelie.Client
import com.example.atelie.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.InternalSerializationApi

object ClientRepository {
    @OptIn(InternalSerializationApi::class)
    suspend fun getClientById(id: Int): Client? {
        return try {
            supabase.from("clients")
                .select() {
                    filter {
                        eq("id", id)
                    }
                }.decodeSingle<Client>()
        } catch (e: Exception) {
            Log.e("Supabase", "Erro ao buscar cliente", e)
            null
        }
    }

     suspend fun getClients(): List<Client> {
        return try {
            supabase
                .from("clients")
                .select()
                .decodeList<Client>()
        } catch (e: Exception) {
            Log.e("Supabase", "Erro ao carregar clientes", e)
            emptyList<Client>()
        }
    }
}