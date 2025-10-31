package com.example.atelie

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.atelie.repositories.ClientRepository
import com.example.atelie.views.ClientCardView
import com.example.atelie.views.ContainerCardView
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi

class ClientsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_clients, container, false)
    }

    private lateinit var cardContainer: ContainerCardView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardContainer = view.findViewById(R.id.card_container)

        val inputSearch = view.findViewById<TextInputEditText>(R.id.input_search)

        inputSearch.doOnTextChanged { text, _, _, _ ->
            val query = text.toString()

            viewLifecycleOwner.lifecycleScope.launch {
                if (query.isNotBlank()) {
                    loadClients(view, searchClient(query))
                } else {
                    loadClients(view, ClientRepository.getClients())
                }
            }

        }

        viewLifecycleOwner.lifecycleScope.launch {
            val clients = ClientRepository.getClients()
            loadClients(view, clients)
        }

        val addClientBtn = view.findViewById<ImageButton>(R.id.add_client)

        addClientBtn.setOnClickListener {
            startActivity(Intent(requireContext(), NewClientActivity::class.java))
        }
    }

    private fun loadClientCard(client: Client): ClientCardView {
        val newClientCard = ClientCardView(requireContext())
        newClientCard.bind(client)
        return newClientCard
    }

    private fun loadClients(view: View, clients: List<Client>) {
        cardContainer.deleteAllCards()

        for ((_, client) in clients.withIndex()) {
            cardContainer.addCard(loadClientCard(client))
        }
    }

    @OptIn(InternalSerializationApi::class)
    suspend fun searchClient(query: String): List<Client> {
        return try {
            supabase.from("clients")
                .select() {
                    filter {
                        or {
                            ilike("name", "%$query%")
                            ilike("phone", "%$query%")
                        }
                    }
                }
                .decodeList<Client>()
        } catch (e: Exception) {
            Log.e("Supabase", "Erro ao buscar clientes", e)
            emptyList()
        }
    }

}
