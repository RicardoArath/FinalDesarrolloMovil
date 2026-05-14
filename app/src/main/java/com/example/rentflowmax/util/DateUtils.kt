package com.example.rentflowmax.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {
    private val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-MX"))

    fun Long.toDisplayDate(): String = displayFormat.format(Date(this))
    fun Long.toMonthYear(): String = monthYearFormat.format(Date(this)).replaceFirstChar { it.uppercase() }

    fun daysUntil(endDateMillis: Long): Long {
        val now = System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(endDateMillis - now)
    }

    fun isExpired(endDateMillis: Long): Boolean = endDateMillis < System.currentTimeMillis()

    fun isExpiringSoon(endDateMillis: Long, days: Int = 30): Boolean {
        val daysLeft = daysUntil(endDateMillis)
        return daysLeft in 0..days
    }

    fun thirtyDaysFromNow(): Long = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30)
}
