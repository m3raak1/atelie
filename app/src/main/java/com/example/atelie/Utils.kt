package com.example.atelie

import android.content.Context


fun dpToPx(dpValue: Int, context: Context): Int {
    return (dpValue * context.resources.displayMetrics.density + 0.5f).toInt()
}

fun formatPhoneNumber(number: String): String {
    // Remove tudo que não for dígito
    val digits = number.filter { it.isDigit() }

    return if (digits.length == 11) {
        val ddd = digits.substring(0, 2)
        val prefixo = digits.substring(2, 7)
        val sufixo = digits.substring(7)
        "($ddd) $prefixo-$sufixo"
    } else {
        number // retorna o original se não tiver 11 dígitos
    }
}
