package edu.upc.openmrs.activities.patientdashboard.charts

import android.os.Build
import androidx.annotation.RequiresApi
import com.github.mikephil.charting.formatter.ValueFormatter

class MyValueFormatter(dates: List<String>) : ValueFormatter() {

    private var mDates: List<String> = dates

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getFormattedValue(value: Float): String =
        mDates.elementAtOrElse(value.toInt()) { return "" }
}
