package edu.upc.openmrs.activities.patientdashboard.charts

import android.os.Build
import androidx.annotation.RequiresApi
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val DATE_PATTERN = "dd/MM/yyyy"
const val DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

class MyValueFormatter(dates: List<String>) : ValueFormatter() {

    private var mDates: List<String> = dates

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getFormattedValue(value: Float): String =
        mDates
            .map { LocalDate.parse(it, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)) }
            .map { it.format(DateTimeFormatter.ofPattern(DATE_PATTERN)) }
            .elementAtOrElse(value.toInt()) { return "" }
}
