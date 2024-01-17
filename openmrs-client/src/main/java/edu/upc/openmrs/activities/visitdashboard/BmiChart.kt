package edu.upc.openmrs.activities.visitdashboard

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import edu.upc.R
import kotlin.math.roundToInt

class BmiChart {

    fun setBMIValueAndChart(bmiValue: Float, vitalsCardView: View) {
        vitalsCardView.findViewById<View>(R.id.bmi_layout).visibility = View.VISIBLE

        val roundedBmi = bmiValue.roundToInt()

        val pointerValue = bmiValue - 10 //10 is the start value of the chart
        val chartRange = 40

        val weight =
            if (roundedBmi >= 50) 1f
            else if (roundedBmi < 10) 0f
            else pointerValue / chartRange

        vitalsCardView.findViewById<TextView>(R.id.bmi_value).text = roundedBmi.toString()

        val spaceBeforePointer = vitalsCardView.findViewById<View>(R.id.bmi_chart_pointer_background)
        val spaceAfterPointer = vitalsCardView.findViewById<View>(R.id.bmi_chart_pointer_background2)

        val spaceBeforeParams = spaceBeforePointer.layoutParams as LinearLayout.LayoutParams
        val spaceAfterParams = spaceAfterPointer.layoutParams as LinearLayout.LayoutParams

        spaceBeforeParams.weight = weight
        spaceAfterParams.weight = 1 - weight

        spaceBeforePointer.layoutParams = spaceBeforeParams
        spaceAfterPointer.layoutParams = spaceAfterParams
    }

}

