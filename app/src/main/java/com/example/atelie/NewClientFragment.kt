package com.example.atelie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.Button
import android.util.Patterns

class NewClientFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_client, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referências aos componentes
        val inputLayoutName = view.findViewById<TextInputLayout>(R.id.input_layout_name)
        val inputName = view.findViewById<TextInputEditText>(R.id.input_name)

        val inputLayoutPhone = view.findViewById<TextInputLayout>(R.id.input_layout_phone)
        val inputPhone = view.findViewById<TextInputEditText>(R.id.input_phone)

        val btnSave = view.findViewById<Button>(R.id.save_client)

        btnSave.setOnClickListener {
            var isValid = true

            // Validação do Nome
            val name = inputName.text.toString().trim()
            if (name.isEmpty()) {
                inputLayoutName.error = "O nome é obrigatório"
                isValid = false
            } else {
                inputLayoutName.error = null
            }

            // Validação do Telefone
            val phone = inputPhone.text.toString().trim()
            if (phone.isEmpty()) {
                inputLayoutPhone.error = "O telefone é obrigatório"
                isValid = false
            } else if (!isValidPhoneNumber(phone)) {
                inputLayoutPhone.error = "Telefone inválido"
                isValid = false
            } else {
                inputLayoutPhone.error = null
            }

            if (isValid) {
                // Aqui você pode continuar com o salvamento ou enviar os dados
            }
        }
    }

    // Função simples de validação de número de telefone (padrão brasileiro simples)
    private fun isValidPhoneNumber(phone: String): Boolean {
        // Exemplo: Aceita formatos como (11) 91234-5678 ou 11912345678
        val regex = Regex("^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}\$")
        return regex.matches(phone)
    }
}
