package com.example.atelie

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton

class NewOrderFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addClotheItem = view.findViewById<MaterialButton>(R.id.add_item_clothe)

        val container = view.findViewById<LinearLayout>(R.id.items_clothe_card)

        addClotheItem.setOnClickListener {
            container.removeView(view.findViewById<LinearLayout>(R.id.placeholder_item_clothe))
            val novoItem = layoutInflater.inflate(R.layout.item_clothe, container, false)
            container.addView(novoItem)
        }
    }
}
