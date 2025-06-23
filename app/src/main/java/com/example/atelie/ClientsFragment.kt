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
import android.widget.ImageButton

class ClientsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_clients, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addClientBtn = view.findViewById<ImageButton>(R.id.add_client)

        addClientBtn.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, NewClientFragment()) // "container" é o id do FrameLayout da sua Activity
                .addToBackStack(null) // Se quiser que o usuário possa voltar
                .commit()
        }
    }
}
