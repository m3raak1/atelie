package com.example.atelie

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class ClientsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_clients, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadClients(view)

        val addClientBtn = view.findViewById<ImageButton>(R.id.add_client)

        addClientBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NewClientFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getClients(onResult: (List<Client>) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = supabase
                    .from("clients")
                    .select()
                    .decodeList<Client>()

                onResult(result)
            } catch (e: Exception) {
                Log.e("Supabase", "Erro ao carregar clientes", e)
            }
        }
    }

    private fun addClientCard(client: Client, container: ViewGroup) {
        val newClientCard = layoutInflater.inflate(R.layout.item_client, container, false)

        newClientCard.findViewById<TextView>(R.id.name_client).text = client.name.replaceFirstChar { it.uppercaseChar() }
        newClientCard.findViewById<TextView>(R.id.phone_client).text = formatPhoneNumber(client.phone)

        container.addView(newClientCard)
    }

    private fun loadClients(view: View) {
        val container = view.findViewById<LinearLayout>(R.id.client_list)

        getClients { clients ->
            for ((index, client) in clients.withIndex()) {
                addClientCard(client, container)

                if (index < clients.lastIndex) {
                    val divider = layoutInflater.inflate(R.layout.view_divider, container, false)
                    container.addView(divider)
                }
            }
        }
    }
}
