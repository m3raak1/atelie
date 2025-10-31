package com.example.atelie

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.contains
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.InternalSerializationApi
import java.util.Calendar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isEmpty
import com.example.atelie.views.ContainerCardView


class NewOrderActivity : AppCompatActivity() {

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

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_order)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.new_order)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.back_btn).setOnClickListener {
            finish()
        }

        clotheItemsController = ClotheItemsController()

        val addClotheItem = findViewById<MaterialButton>(R.id.add_item_clothe)

        addClotheItem.setOnClickListener {
            val itemCard = setupItemClotheDialog()
            itemCard.dialog.show(supportFragmentManager, "FullScreenDialog")
        }

        findViewById<ContainerCardView>(R.id.card_container).setOnEmptyListener {
            findViewById<TextView>(R.id.total_price).visibility = View.INVISIBLE
        }

        val inputClient = findViewById<MaterialAutoCompleteTextView>(R.id.input_client)

        initializeClientsInAutocomplete(inputClient) { erro ->
            Toast.makeText(this, "Erro ao carregar clientes", Toast.LENGTH_SHORT).show()
        }


        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val name = result.data?.getStringExtra("name") ?: return@registerForActivityResult
                val phone = result.data?.getStringExtra("phone") ?: return@registerForActivityResult

                inputClient.setText("$name - $phone")

                lifecycleScope.launch {
                    initializeClientsInAutocomplete(inputClient) { erro ->
                        Toast.makeText(this@NewOrderActivity, "Erro ao carregar clientes", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        findViewById<ImageButton>(R.id.new_client_btn).setOnClickListener {
            launcher.launch(Intent(this, NewClientActivity::class.java))
        }

        val inputData = findViewById<TextInputEditText>(R.id.input_data_entry)

        inputData.setOnClickListener {
            val calendario = Calendar.getInstance()
            val ano = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, anoSelecionado, mesSelecionado, diaSelecionado ->
                val dataFormatada = "%02d/%02d/%04d".format(diaSelecionado, mesSelecionado + 1, anoSelecionado)
                inputData.setText(dataFormatada)
            }, ano, mes, dia)

            datePicker.show()
        }

        val sendDataBtn = findViewById<MaterialButton>(R.id.save_client);
        sendDataBtn.setOnClickListener {
            lifecycleScope.launch {
                if (validateInputs()) {
                    sendOrder()
                    finish()
                }
            }
        }

        val clientString = intent.getStringExtra("clientString")
        if (!clientString.isNullOrEmpty()) {
            inputClient.setText(clientString)
        }
    }

    private var clienteMap: Map<String, Client> = emptyMap()

    suspend fun updateClientMap() {
        val clientes = supabase
            .from("clients")
            .select()
            .decodeList<Client>()

        clienteMap = clientes.associateBy {
            val nome = it.name ?: "Sem nome"
            val tel = it.phone ?: "Sem telefone"
            "$nome - $tel"
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun initializeClientsInAutocomplete(
        autoCompleteTextView: MaterialAutoCompleteTextView,
        onError: ((Throwable) -> Unit)? = null
    ) {
        lifecycleScope.launch {
            try {
                updateClientMap()

                val nomesClientes = clienteMap.keys.toList()
                val adapter = ArrayAdapter(this@NewOrderActivity, R.layout.item_client_dropdown, nomesClientes)
                autoCompleteTextView.setAdapter(adapter)

            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("Supabase", "Erro ao carregar clientes")
                onError?.invoke(e)
            }
        }
    }

    private fun setupItemClotheDialog(): ItemClotheCard {
        val container = findViewById<ContainerCardView>(R.id.card_container)

        val itemClotheCard = ItemClotheCard(
            layoutInflater.inflate(R.layout.item_clothe_card, container, false),
            FullScreenDialog()
        )

        itemClotheCard.dialog.setOnResultListener { resultado ->
            itemClotheCard.data = resultado

            if (!itemClothesList.contains(itemClotheCard)) {
                itemClothesList.add(itemClotheCard)
                additemClothe(itemClotheCard)
            } else {
                updateItemClotheCard(itemClotheCard)
            }
        }

        itemClotheCard.dialog.setOnDeleteListener {
            findViewById<ContainerCardView>(R.id.card_container).deleteCard(itemClotheCard.card)
            itemClothesList.remove(itemClotheCard)
        }

        itemClotheCard.card.setOnClickListener {
            itemClotheCard.dialog.show(supportFragmentManager, "FullScreenDialog")
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

    private fun additemClothe(itemClothe: ItemClotheCard) {
        updateItemClotheCard(itemClothe)

        val container = findViewById<ContainerCardView>(R.id.card_container)
        container.addCard(itemClothe.card)

        val totalValueView = findViewById<TextView>(R.id.total_price)
        totalValueView.visibility = View.VISIBLE

        var totalValue = 0.00F

        for (itemClothe in itemClothesList) {
            if (itemClothe.data == null) throw IllegalArgumentException("O cardData não pode ser nulo")
            val data = itemClothe.getDataOrThrow()
            totalValue += data.price * data.quantity
        }

        totalValueView.text = "Total: ${totalValue} R$"
    }

    suspend fun validateInputs(): Boolean {
        var isValid = true

        val client = findViewById<MaterialAutoCompleteTextView>(R.id.input_client)
        val clientLayout = findViewById<TextInputLayout>(R.id.input_layout_client)
        if (client.text.isNullOrBlank()) {
            clientLayout.error = "Este campo é obrigatório."
            isValid = false
        } else if (clienteMap[client.text.toString()] == null) {
            clientLayout.error = "Este cliente é invalido."
            isValid = false
        } else {
            clientLayout.error = null
        }

        val positionLayout = findViewById<TextInputLayout>(R.id.input_layout_position)
        val position = findViewById<TextInputEditText>(R.id.input_position)
        if (position.text.toString().toIntOrNull() == null) {
            positionLayout.error = "Este campo é obrigatório"
            isValid = false
        } else if (position.text.toString().toInt() > 140 || position.text.toString().toInt() < 1) {
            positionLayout.error = "Digite um valor entre 1 e 140"
            isValid = false
        } else {
            try {
                val result = supabase.from("orders")
                    .select {
                        filter {
                            eq("position", position.text.toString().toInt())
                            exact("exited_at", null)
                        }
                        count(Count.EXACT)
                    }.countOrNull()

                if (result!!.toInt() != 0) {
                    positionLayout.error = "Posição já ocupada"
                    isValid = false
                } else {
                    positionLayout.error = null
                }

                Log.d("Supabase", result.toString())
            } catch (e: Exception) {
                Log.e("Supabase", "Erro ao enviar peças", e)
            }
        }

        val dateLayout = findViewById<TextInputLayout>(R.id.input_layout_data_entry)
        val date = findViewById<TextInputEditText>(R.id.input_data_entry).text
        if (date.isNullOrBlank()) {
            dateLayout.error = "Este campo é obrigatório"
            isValid = false
        } else if (datePastCheck(toLocalDate(date.toString())!!)) {
            dateLayout.error = "Data no passado"
            isValid = false
        } else {
            dateLayout.error = null
        }

        val timeLayout = findViewById<TextInputLayout>(R.id.input_layout_time)
        val time = findViewById<AutoCompleteTextView>(R.id.input_time)
        if (time.text.isNullOrBlank()) {
            timeLayout.error = "Este campo é obrigatório"
            isValid = false
        } else {
            timeLayout.error = null
        }

        val itemClotheCard = findViewById<MaterialCardView>(R.id.items_clothe_card)
        val itemClothesError = findViewById<TextView>(R.id.item_clothe_error)

        if (itemClothesList.isEmpty()) {
            itemClothesError.visibility = View.VISIBLE
            itemClotheCard.strokeColor = ContextCompat.getColor(this, com.google.android.material.R.color.design_default_color_error)
            isValid = false
        } else {
            itemClothesError.visibility = View.GONE
            itemClotheCard.strokeColor = ContextCompat.getColor(this, R.color.border)
        }

        return isValid
    }

    fun sendOrder() {
        val client = clienteMap[findViewById<AutoCompleteTextView>(R.id.input_client).text.toString()]

        val position = findViewById<TextInputEditText>(R.id.input_position).text.toString().toInt()

        var totalPrice = 0F
        for (itemClothe in itemClothesList) {
            if (itemClothe.data == null) throw IllegalArgumentException("O cardData não pode ser nulo")

            val data = itemClothe.getDataOrThrow()
            totalPrice += data.price * data.quantity
        }

        val statusPayment = findViewById<CheckBox>(R.id.payment).isChecked

        val exitDateText = findViewById<TextInputEditText>(R.id.input_data_entry).text.toString()
        val partes = exitDateText.split("/")
        val exitDate = LocalDate(
            partes[2].toInt(),
            partes[1].toInt(),
            partes[0].toInt()
        )

        val order = OrderToDb(client!!.id, position, totalPrice, statusPayment, exitDate)

        lifecycleScope.launch {
            try {
                val result = supabase
                    .from("orders")
                    .insert(order) {
                        select()
                    }.decodeSingle<Order>()

                sendItemsClothe(result)

                Log.d("Supabase", result.toString())
                Toast.makeText(this@NewOrderActivity, "Pedido enviado com sucesso", Toast.LENGTH_SHORT).show()

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

        lifecycleScope.launch {
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

        lifecycleScope.launch {
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