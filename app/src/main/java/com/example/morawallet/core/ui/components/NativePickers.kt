package com.example.morawallet.core.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import java.util.Calendar

fun showNativeDatePicker(
    context: Context,
    initialMillis: Long,
    maxMillis: Long = System.currentTimeMillis(),
    onSelected: (Long) -> Unit,
) {
    val initial = Calendar.getInstance().apply { timeInMillis = initialMillis }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            val picked = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onSelected(picked.timeInMillis)
        },
        initial.get(Calendar.YEAR),
        initial.get(Calendar.MONTH),
        initial.get(Calendar.DAY_OF_MONTH),
    ).apply {
        datePicker.maxDate = maxMillis
        show()
    }
}

fun showNativeTimePicker(
    context: Context,
    initialMillis: Long,
    onSelected: (hour: Int, minute: Int) -> Unit,
) {
    val initial = Calendar.getInstance().apply { timeInMillis = initialMillis }
    TimePickerDialog(
        context,
        { _, hour, minute -> onSelected(hour, minute) },
        initial.get(Calendar.HOUR_OF_DAY),
        initial.get(Calendar.MINUTE),
        true,
    ).show()
}
