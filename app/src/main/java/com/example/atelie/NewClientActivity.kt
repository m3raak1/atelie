package com.example.atelie

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.coroutines.launch

class NewClientActivity : AppCompatActivity() {
    private lateinit var inputName: TextInputEditText
    private lateinit var inputPhone: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_client)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.back_btn).setOnClickListener {
            finish()
        }
        inputName = findViewById<TextInputEditText>(R.id.input_name)
        inputPhone = findViewById<TextInputEditText>(R.id.input_phone)

        val btnSave = findViewById<Button>(R.id.save_client)

        btnSave.setOnClickListener {
            lifecycleScope.launch {
                if (verifyPhone()) {
                    sendClient(inputsToClient()) {
                        val resultIntent = Intent()
                        resultIntent.putExtra("name", inputName.text.toString())
                        resultIntent.putExtra("phone", inputPhone.text.toString())
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                }
            }
        }
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        val regex = Regex("^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}\$")
        return regex.matches(phone)
    }

    private suspend fun verifyPhone(): Boolean {
        val inputLayoutPhone = findViewById<TextInputLayout>(R.id.input_layout_phone)
        val inputLayoutName = findViewById<TextInputLayout>(R.id.input_layout_name)

        var isValid = true

        val name = inputName.text.toString().trim()
        if (name.isEmpty()) {
            inputLayoutName.error = "O nome é obrigatório"
            isValid = false
        } else {
            inputLayoutName.error = null
        }

        val phone = inputPhone.text.toString().trim()
        if (phone.isEmpty()) {
            inputLayoutPhone.error = "O telefone é obrigatório"
            isValid = false
        } else if (!isValidPhoneNumber(phone)) {
            inputLayoutPhone.error = "Telefone inválido"
            isValid = false
        } else {
            try {
                val result = supabase.from("clients")
                    .select {
                        filter {
                            eq("phone", phone)
                        }
                        count(Count.EXACT)
                    }.countOrNull()

                if (result!!.toInt() != 0) {
                    inputLayoutPhone.error = "Número já registrado"
                    isValid = false
                } else {
                    inputLayoutPhone.error = null

                }

                Log.d("Supabase", result.toString())
            } catch (e: Exception) {
                Log.e("Supabase", "Erro ao enviar peças", e)
            }
        }

        return isValid
    }

    private fun inputsToClient(): ClientToDb {
        return ClientToDb(inputName.text.toString().lowercase(), inputPhone.text.toString())
    }

    private fun sendClient(client: ClientToDb, onSuccess: () -> Unit) {
        lifecycleScope.launch {
            try {
                supabase
                    .from("clients")
                    .insert(client)

                Toast.makeText(this@NewClientActivity, "Cliente adicionado com sucesso", Toast.LENGTH_SHORT).show()
                onSuccess()
            } catch (e: Exception) {
                Log.e("Supabase", "Falha ao enviar cliente", e)
            }
        }
    }
}