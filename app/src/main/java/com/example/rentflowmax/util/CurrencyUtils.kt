package com.example.rentflowmax.util

import java.text.NumberFormat
import java.util.*

object CurrencyUtils {
    private val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX"))

    fun Double.toCurrencyString(): String = format.format(this)
}
