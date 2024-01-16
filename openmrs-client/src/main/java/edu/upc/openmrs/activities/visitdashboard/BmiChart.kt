package edu.upc.openmrs.activities.visitdashboard

import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import edu.upc.R

class BmiChart {

    companion object {

        @JvmStatic
        fun setBMIValueAndChart(bmiValue: String, vitalsCardView: View) {
            if (bmiValue != "N/A") {
                vitalsCardView.findViewById<View>(R.id.bmi_layout).visibility = View.VISIBLE

                val chartSizeInPx = 1000
                val bmiRange = 40
                val bmiStartValue = 10

                val pointerPositionBasedOnBmiValueAndChartSize =
                    if (bmiValue.toFloat() > 50) chartSizeInPx - 10
                    else ((bmiValue.toFloat() - bmiStartValue) / bmiRange * chartSizeInPx).toInt()

                val pointer = vitalsCardView.findViewById<View>(R.id.bmi_chart_pointer)
                val value = vitalsCardView.findViewById<TextView>(R.id.bmi_value)

                val params = pointer.layoutParams as RelativeLayout.LayoutParams
                params.marginStart = pointerPositionBasedOnBmiValueAndChartSize
                pointer.layoutParams = params
                value.text = bmiValue

                val params2 = value.layoutParams as LinearLayout.LayoutParams
                params2.marginStart = pointerPositionBasedOnBmiValueAndChartSize - 35
                value.layoutParams = params2
            }
        }

    }
}
