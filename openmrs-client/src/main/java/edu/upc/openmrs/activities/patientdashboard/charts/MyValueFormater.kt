package edu.upc.openmrs.activities.patientdashboard.charts

import android.os.Build
import androidx.annotation.RequiresApi
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import edu.upc.openmrs.activities.patientdashboard.charts.ChartsViewActivity.Companion.DATE_TIME_PATTERN
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MyValueFormatter(dates: List<String>) : ValueFormatter() {

    private var mDates: List<String> = dates

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getFormattedValue(value: Float): String =
        mDates
            .map { LocalDate.parse(it, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)) }
            .map { it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }
            .elementAtOrElse(value.toInt()) { return "" }
}
