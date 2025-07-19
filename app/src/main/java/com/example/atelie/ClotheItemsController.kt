package com.example.atelie

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import okio.Inflater
import java.time.OffsetDateTime

class ClotheItemsController {

    @Serializable
    @OptIn(InternalSerializationApi::class)
    data class ClothesType(
        val id: Int,
        val name: String,
        @Serializable(with = OffsetDateTimeSerializer::class)
        val created_at: OffsetDateTime
    )

    private val clothesItems = mutableListOf<View>()

    fun addItemClothe(
        container: LinearLayout,
        context: Context,
        inflater: LayoutInflater,
        lifecycleOwner: LifecycleOwner
    ) {
        val lastItem = clothesItems.lastOrNull()
        if (lastItem != null && !validateItem(lastItem)) {
            Toast.makeText(context, "Preencha os campos antes de adicionar outro", Toast.LENGTH_SHORT).show()
            return
        }

        // Remove placeholder se existir


        val scrollView = container.parent as? HorizontalScrollView

        scrollView?.post {
            val width = scrollView.width
            val newItem = inflater.inflate(R.layout.item_clothe, container, false)

            newItem.layoutParams = LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT)

            initializeClothesDropdown(newItem, lifecycleOwner, context) {
                Toast.makeText(context, "Erro ao carregar tipos de roupas", Toast.LENGTH_SHORT).show()
            }

            container.addView(newItem)
            clothesItems.add(newItem)
        }

        container.findViewById<MaterialCardView?>(R.id.item_clothe_placeholder)?.let {
            container.removeView(it)
        }
    }


    private fun initializeClothesDropdown(
        itemView: View,
        lifecycleOwner: LifecycleOwner,
        context: Context,
        onError: (Throwable) -> Unit = {}
    ) {
        val autoComplete = itemView.findViewById<MaterialAutoCompleteTextView>(R.id.input_clothes)

        lifecycleOwner.lifecycleScope.launch {
            try {
                val clothes = supabase
                    .from("types_clothing")
                    .select()
                    .decodeList<ClothesType>()

                val nomes = clothes.map {
                    it.name.replaceFirstChar { c -> c.uppercaseChar() }
                }

                val adapter = ArrayAdapter(context, R.layout.item_client_dropdown, nomes)
                autoComplete.setAdapter(adapter)

            } catch (e: Exception) {
                Log.e("Supabase", "Erro ao carregar tipos de roupas", e)
                onError(e)
            }
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

    fun getAllItems(): List<View> = clothesItems
}
