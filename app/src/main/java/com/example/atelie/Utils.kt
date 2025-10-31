package com.example.atelie

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


fun dpToPx(dpValue: Int, context: Context): Int {
    return (dpValue * context.resources.displayMetrics.density + 0.5f).toInt()
}

fun formatPhoneNumber(number: String): String {
    val digits = number.filter { it.isDigit() }

    return if (digits.length == 11) {
        val ddd = digits.substring(0, 2)
        val prefixo = digits.substring(2, 7)
        val sufixo = digits.substring(7)
        "($ddd) $prefixo-$sufixo"
    } else {
        number
    }
}

fun toLocalDate(date: String, pattern: String = "dd/MM/yyyy"): LocalDate? =
    try {
        LocalDate.parse(date, DateTimeFormatter.ofPattern(pattern))
    } catch (e: DateTimeParseException) {
        null
    }

@SuppressLint("DefaultLocale")
fun toStringDate(date: kotlinx.datetime.LocalDate): String {
    return "${String.format("%02d",date.dayOfMonth)}/${String.format("%02d",date.monthNumber)}/${date.year}"
}

@SuppressLint("DefaultLocale")
fun toStringDate(date: OffsetDateTime): String {
    return "${String.format("%02d",date.dayOfMonth)}/${String.format("%02d",date.monthValue)}/${date.year}"
}

fun datePastCheck(date: LocalDate): Boolean = date.isBefore(LocalDate.now())
