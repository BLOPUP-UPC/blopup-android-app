package edu.upc.openmrs.activities.patientdashboard.charts

import android.os.Build
import androidx.annotation.RequiresApi
import com.github.mikephil.charting.formatter.ValueFormatter


class TreatmentFormater(val name: String) : ValueFormatter() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getFormattedValue(value: Float): String = name
}
