package edu.upc.openmrs.activities.patientdashboard.charts

import android.os.Build
import androidx.annotation.RequiresApi
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class MyValueFormatter(values: List<String>) : ValueFormatter() {

    private var mValues : List<String> = values

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return mValues[value.toInt()]
    }
}

