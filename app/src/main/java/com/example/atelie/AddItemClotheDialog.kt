package com.example.atelie

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isNotEmpty
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

class FullScreenDialog : DialogFragment() {

    @Serializable
    @OptIn(InternalSerializationApi::class)
    data class ClothesType(
        val id: Int,
        val name: String,
        @Serializable(with = OffsetDateTimeSerializer::class)
        val created_at: OffsetDateTime
    )

    @Serializable
    @OptIn(InternalSerializationApi::class)
    data class Services(
        val id: Int,
        val name: String,
        @Serializable(with = OffsetDateTimeSerializer::class)
        val created_at: OffsetDateTime
    )

    data class ItemClotheDialogData(
        val ids_service: IntArray = intArrayOf(),
        val names_service: Array<String> = arrayOf(),
        val id_clothing_type: Int,
        val name_clothing_type: String,
        val desc: String,
        val price: Float,
        val quantity: Int
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_item_clothe_dialog, container, false)
    }

    var checkedBoxesId = mutableListOf<Int>()

    var clotheTypeId: Int? = null
    var clotheTypeName: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val addButton = view.findViewById<MaterialButton>(R.id.add_item_clothe_btn)

        addButton.setOnClickListener {
            enviarResultado(view)
        }

        var selectedClothesType: ClothesType? = null

        initializeClothesDropdown(view, viewLifecycleOwner, requireContext(), onItemSelected = { selected ->
            selectedClothesType = selected
            if (selected != null) {
                clotheTypeId = selected.id
                clotheTypeName = selected.name
                Log.d("Clothes", "Selecionado: ${selected.name} (id=${selected.id})")
            } else {
                clotheTypeId = null
                Log.w("Clothes", "Valor digitado inválido")
            }
        })

        getServices(viewLifecycleOwner) { services ->
            setupServiceSelects(services, view, requireContext())
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.white) // Ou transparente
    }

    private fun initializeClothesDropdown(
        itemView: View,
        lifecycleOwner: LifecycleOwner,
        context: Context,
        onItemSelected: (ClothesType?) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        val autoComplete = itemView.findViewById<MaterialAutoCompleteTextView>(R.id.input_clothes)

        lifecycleOwner.lifecycleScope.launch {
            try {
                val clothes = supabase
                    .from("types_clothing")
                    .select()
                    .decodeList<ClothesType>()

                val nameMap = clothes.associateBy { it.name.replaceFirstChar { c -> c.uppercaseChar() } }
                val nomes = nameMap.keys.toList()

                val adapter = ArrayAdapter(context, R.layout.item_client_dropdown, nomes)
                autoComplete.setAdapter(adapter)

                autoComplete.doOnTextChanged { text, _, _, _ ->
                    val texto = text?.toString()?.replaceFirstChar { it.uppercaseChar() }
                    val roupa = nameMap[texto]
                    onItemSelected(roupa)
                }

            } catch (e: Exception) {
                Log.e("Supabase", "Erro ao carregar tipos de roupas", e)
                onError(e)
            }
        }
    }

    private fun getServices(
        lifecycleOwner: LifecycleOwner,
        callback: (services: List<Services>) -> Unit
    ) {
        lifecycleOwner.lifecycleScope.launch {
            try {
                val services = supabase
                    .from("services")
                    .select()
                    .decodeList<Services>()

                callback(services)
            } catch (e: Exception) {
                Log.e("Supabase", "Erro ao carregar serviços", e)
            }
        }
    }

    private fun setupServiceSelects(
        services: List<Services>,
        itemView: View,
        context: Context
    ) {
        val checkBoxContainer = itemView.findViewById<LinearLayout>(R.id.checkbox_cotainer)
        var linearLayout: LinearLayout? = null

        for ((i, services) in services.withIndex()) {
            if (i % 3 == 0) {
                linearLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                checkBoxContainer.addView(linearLayout)
            }

            val checkBox = CheckBox(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, dpToPx(10, context), dpToPx(10, context))
                    isChecked = checkedBoxesId.contains(services.id)
                }

                text = services.name
                tag = services.id

                setTextColor(ContextCompat.getColor(context, R.color.black)) // opcional
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)

                buttonTintList = ContextCompat.getColorStateList(context, R.color.checkbox_tint)

                isSingleLine = true
            }

            checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    checkedBoxesId.add(services.id)
                } else {
                    checkedBoxesId.remove(services.id)
                }
            }

            linearLayout?.addView(checkBox)
        }
    }


    fun getCheckedCheckboxes(container: ViewGroup): List<CheckBox> {
        val checkedList = mutableListOf<CheckBox>()

        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)

            when (child) {
                is CheckBox -> {
                    if (child.isChecked) checkedList.add(child)
                }
                is ViewGroup -> {
                    checkedList.addAll(getCheckedCheckboxes(child))
                }
            }
        }

        return checkedList
    }


    fun validateItem(view: View): Boolean {
        var isValid = true

        val clothesTypeInput = view.findViewById<MaterialAutoCompleteTextView>(R.id.input_clothes)
        val clothesTypeLayout = view.findViewById<TextInputLayout>(R.id.input_layout_clothe_type)

        if (clothesTypeInput.text.isNullOrBlank()) {
            clothesTypeLayout.error = "Este campo é obrigatório"
            isValid = false
        } else if (clotheTypeId == null) {
            clothesTypeLayout.error = "Tipo inválido"
            isValid = false
        } else {
            clothesTypeLayout.error = null
        }


        val checkBoxContainer = view.findViewById<LinearLayout>(R.id.checkbox_cotainer)
        val checkBoxError = view.findViewById<LinearLayout>(R.id.text_error_services)
        if (getCheckedCheckboxes(checkBoxContainer).isEmpty()) {
            checkBoxError.visibility = View.VISIBLE
            isValid = false
        } else {
            checkBoxError.visibility = View.GONE
        }

        val priceInput = view.findViewById<TextInputEditText>(R.id.input_clothe_price)
        val priceLayout = view.findViewById<TextInputLayout>(R.id.input_layout_clothe_price)
        if (priceInput.text.isNullOrBlank()) {
            priceLayout.error = "Este campo é obrigatorio"
            isValid = false
        } else {
            priceLayout.error = null
        }

        return isValid
    }

    private var onResult: ((ItemClotheDialogData) -> Unit)? = null

    fun setOnResultListener(listener: (ItemClotheDialogData) -> Unit) {
        onResult = listener
    }

    // quando quiser retornar algo, tipo ao clicar em "Salvar"
    private fun enviarResultado(view: View) {
        if (!validateItem(view)) return

        val checkedBoxes = getCheckedCheckboxes(view.findViewById<LinearLayout>(R.id.checkbox_cotainer))
        val checkBoxesId = checkedBoxes.mapNotNull { it.tag as? Int }.toIntArray()
        val checkBoxesName = checkedBoxes.mapNotNull { it.text as? String }.toTypedArray()

        val desc = view.findViewById<TextInputEditText>(R.id.input_clothe_desc).text.toString()
        val price = view.findViewById<TextInputEditText>(R.id.input_clothe_price).text.toString().toFloat()
        val quantity = view.findViewById<TextInputEditText>(R.id.input_clothe_quantity).text.toString().toInt()

        val resultado = ItemClotheDialogData(
            ids_service = checkBoxesId,
            names_service = checkBoxesName,
            id_clothing_type = clotheTypeId!!,
            name_clothing_type = clotheTypeName!!,
            desc = desc,
            price = price,
            quantity = quantity
        ) // monta o objeto com os dados que quer devolver
        onResult?.invoke(resultado)
        dismiss()
    }
}
