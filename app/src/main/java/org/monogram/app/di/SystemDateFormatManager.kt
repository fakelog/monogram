package org.monogram.app.di

import android.content.Context
import android.text.format.DateFormat
import org.monogram.core.date.DateFormatManager

class SystemDateFormatManager(
    private val context: Context,
) : DateFormatManager {
    override fun is24HourFormat(): Boolean = DateFormat.is24HourFormat(context)

    override fun getHourMinuteFormat(): String = if (is24HourFormat()) "HH:mm" else "h:mm a"
}
