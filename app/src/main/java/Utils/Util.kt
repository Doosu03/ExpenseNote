package com.example.expensenote.Utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.expensenote.entity.DateRange
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

object Util {

    fun formatCurrency(value: Double, locale: Locale = Locale.getDefault()): String =
        NumberFormat.getCurrencyInstance(locale).format(value)

    @RequiresApi(Build.VERSION_CODES.O)
    fun isInRange(date: LocalDate, range: DateRange): Boolean {
        val afterFrom = range.from?.let { !date.isBefore(it) } ?: true
        val beforeTo = range.to?.let { !date.isAfter(it) } ?: true
        return afterFrom && beforeTo
    }
}
