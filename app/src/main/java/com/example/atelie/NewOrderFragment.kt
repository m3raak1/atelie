package com.example.atelie

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.contains
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.InternalSerializationApi
import java.util.Calendar

class NewOrderFragment : Fragment() {

    private lateinit var clotheItemsController: ClotheItemsController

    private data class ItemClotheCard(
        val card: View,
        val dialog: FullScreenDialog,
        var data: FullScreenDialog.ItemClotheDialogData? = null
    ) {
        fun getDataOrThrow(): FullScreenDialog.ItemClotheDialogData {
            return data ?: throw IllegalArgumentException("O cardData não pode ser nulo")
        }
    }

    private var itemClothesList = mutableListOf<ItemClotheCard>()

    var selectedClient: Client? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_order, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clotheItemsController = ClotheItemsController()

        val addClotheItem = view.findViewById<MaterialButton>(R.id.add_item_clothe)

        addClotheItem.setOnClickListener {
            val itemCard = setupItemClotheDialog(view)
            itemCard.dialog.show(parentFragmentManager, "FullScreenDialog")
        }

        val inputClient = view.findViewById<MaterialAutoCompleteTextView>(R.id.input_client)

        initializeClientsInAutocomplete(view, inputClient) { erro ->
            Toast.makeText(requireContext(), "Erro ao carregar clientes", Toast.LENGTH_SHORT).show()
        }

        val inputData = view.findViewById<TextInputEditText>(R.id.input_data_entry)

        inputData.setOnClickListener {
            val calendario = Calendar.getInstance()
            val ano = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, anoSelecionado, mesSelecionado, diaSelecionado ->
                val dataFormatada = "%02d/%02d/%04d".format(diaSelecionado, mesSelecionado + 1, anoSelecionado)
                inputData.setText(dataFormatada)
            }, ano, mes, dia)

            datePicker.show()
        }

        val sendDataBtn = view.findViewById<MaterialButton>(R.id.save_client);
        sendDataBtn.setOnClickListener {
            sendOrder(view)
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun initializeClientsInAutocomplete(
        view: View,
        autoCompleteTextView: MaterialAutoCompleteTextView,
        onError: ((Throwable) -> Unit)? = null
    ) {
        val contexto = view.context

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val clientes = supabase
                    .from("clients")
                    .select()
                    .decodeList<Client>()

                val clienteMap = clientes.associateBy {
                    val nome = it.name ?: "Sem nome"
                    val tel = it.phone ?: "Sem telefone"
                    "$nome - $tel"
                }

                val nomesClientes = clienteMap.keys.toList()
                val adapter = ArrayAdapter(contexto, R.layout.item_client_dropdown, nomesClientes)
                autoCompleteTextView.setAdapter(adapter)

                autoCompleteTextView.doOnTextChanged { text, _, _, _ ->
                    val selected = text.toString()
                    val cliente = clienteMap[selected]
                    if (cliente != null) {
                        selectedClient = cliente
                    } else {
                        selectedClient = null
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("Supabase", "Erro ao carregar clientes")
                onError?.invoke(e)
            }
        }
    }

    private fun setupItemClotheDialog(view: View): ItemClotheCard {
        val container = view.findViewById<LinearLayout>(R.id.item_clothe_list)

        val itemClotheCard = ItemClotheCard(
            layoutInflater.inflate(R.layout.item_clothe_card, container, false),
            FullScreenDialog()
        )

        itemClotheCard.dialog.setOnResultListener { resultado ->
            itemClotheCard.data = resultado

            val container = view.findViewById<LinearLayout>(R.id.item_clothe_list)
            if (!container.contains(itemClotheCard.card)) {
                itemClothesList.add(itemClotheCard)
                additemClothe(view, itemClotheCard)
            } else {
                updateItemClotheCard(itemClotheCard)
            }
        }

        itemClotheCard.card.setOnClickListener {
            itemClotheCard.dialog.show(parentFragmentManager, "FullScreenDialog")
        }

        return itemClotheCard
    }

    private fun updateItemClotheCard(itemClothe: ItemClotheCard) {
        val data = itemClothe.getDataOrThrow()

        itemClothe.card.findViewById<TextView>(R.id.title).text = data.name_clothing_type.replaceFirstChar { it.uppercaseChar() }
        itemClothe.card.findViewById<TextView>(R.id.desc).text = data.desc

        val servicesString = data.names_service.joinToString(" - ") {
            it.replaceFirstChar { c -> c.uppercaseChar() }
        }

        itemClothe.card.findViewById<TextView>(R.id.services).text = servicesString
        itemClothe.card.findViewById<TextView>(R.id.price).text = "${data.price} R$"
        itemClothe.card.findViewById<TextView>(R.id.quantity).text = "${data.quantity}x"
    }

    private fun additemClothe(view: View, itemClothe: ItemClotheCard) {
        updateItemClotheCard(itemClothe)

        val container = view.findViewById<LinearLayout>(R.id.item_clothe_list)
        val placeholder = view.findViewById<TextView>(R.id.item_clothe_placeholder)

        container.removeView(placeholder)

        if (container.childCount >= 1) {
            val divider = layoutInflater.inflate(R.layout.view_divider, container, false)
            container.addView(divider)
        }

        val totalValueView = view.findViewById<TextView>(R.id.total_price)
        totalValueView.visibility = View.VISIBLE

        var totalValue = 0.00F

        for (itemClothe in itemClothesList) {
            if (itemClothe.data == null) throw IllegalArgumentException("O cardData não pode ser nulo")
            val data = itemClothe.getDataOrThrow()
            totalValue += data.price * data.quantity
        }

        totalValueView.text = "Total: ${totalValue} R$"

        container.addView(itemClothe.card)
    }

    fun validateInputs(view: View): Boolean {
        var isValid = true

        val client = view.findViewById<MaterialAutoCompleteTextView>(R.id.input_client)
        val clientLayout = view.findViewById<TextInputLayout>(R.id.input_layout_client)
        if (client.text.isNullOrBlank()) {
            clientLayout.error = "Este campo é obrigatório"
            isValid = false
        } else {
            clientLayout.error = null
        }

        val positionLayout = view.findViewById<TextInputLayout>(R.id.input_layout_position)
        val position = view.findViewById<TextInputEditText>(R.id.input_position)
        if (position.text.toString().toIntOrNull() == null) {
            positionLayout.error = "Este campo é obrigatório"
            isValid = false
        } else if (position.text.toString().toInt() > 140 || position.text.toString().toInt() < 1) {
            positionLayout.error = "Digite um valor entre 1 e 140"
            isValid = false
        } else {
            positionLayout.error = null
        }

        val dateLayout = view.findViewById<TextInputLayout>(R.id.input_layout_data_entry)
        val date = view.findViewById<TextInputEditText>(R.id.input_data_entry)
        if (date.text.isNullOrBlank()) {
            dateLayout.error = "Este campo é obrigatório"
            isValid = false
        } else {
            dateLayout.error = null
        }

        val timeLayout = view.findViewById<TextInputLayout>(R.id.input_layout_time)
        val time = view.findViewById<AutoCompleteTextView>(R.id.input_time)
        if (time.text.isNullOrBlank()) {
            timeLayout.error = "Este campo é obrigatório"
            isValid = false
        } else {
            timeLayout.error = null
        }

        return isValid
    }

    fun sendOrder(view: View) {
        if (!validateInputs(view)) return

        val position = view.findViewById<TextInputEditText>(R.id.input_position).text.toString().toInt()

        var totalPrice = 0F
        for (itemClothe in itemClothesList) {
            if (itemClothe.data == null) throw IllegalArgumentException("O cardData não pode ser nulo")

            val data = itemClothe.getDataOrThrow()
            totalPrice += data.price * data.quantity
        }

        val statusPayment = view.findViewById<CheckBox>(R.id.payment).isChecked

        val exitDateText = view.findViewById<TextInputEditText>(R.id.input_data_entry).text.toString()
        val partes = exitDateText.split("/")
        val exitDate = LocalDate(
            partes[2].toInt(),
            partes[1].toInt(),
            partes[0].toInt()
        )

        val order = OrderToDb(selectedClient!!.id, position, totalPrice, statusPayment, exitDate)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = supabase
                    .from("orders")
                    .insert(order) {
                        select()
                    }.decodeSingle<Order>()

                sendItemsClothe(result)

                Log.d("Supabase", result.toString())
                Toast.makeText(context, "Pedido enviado com sucesso", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Log.e("Supabase", "Erro ao enviar pedido", e)
            }
        }

    }

    fun sendItemsClothe(order: Order) {
        val itemsList = mutableListOf<ItemClothingToDb>()

        for (itemClothe in itemClothesList) {
            if (itemClothe.data == null) throw IllegalArgumentException("O cardData não pode ser nulo")
            val data = itemClothe.getDataOrThrow()

            itemsList.add(ItemClothingToDb(
                order.id,
                data.id_clothing_type,
                order.id_client,
                data.desc,
                data.price
            ))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = supabase
                    .from("items_clothing")
                    .insert(itemsList) {
                        select()
                    }.decodeList<ItemClothing>()

                Log.e("Supabase", result.toString())

                sendServices(result)
            } catch (e: Exception) {
                Log.e("Supabase", "Erro ao enviar peças", e)
            }
        }
    }

    fun sendServices(itemClothingList: List<ItemClothing>) {
        val servicesRelationList = mutableListOf<ItemClothingService>()

        itemClothesList.forEachIndexed { index, itemClothing ->
            val data = itemClothing.getDataOrThrow()
            data.ids_service.forEach { idService ->
                servicesRelationList.add(ItemClothingService(itemClothingList[index].id!!, idService))
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                supabase
                    .from("item_clothing_service")
                    .insert(servicesRelationList)
            } catch (e: Exception) {
                Log.e("Supabase", "Erro ao linkar serviços", e)
            }
        }
    }
}
