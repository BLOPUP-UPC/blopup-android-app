package edu.upc.openmrs.activities.visitdashboard

import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.solver.state.Dimension
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

        val blankSpace = vitalsCardView.findViewById<View>(R.id.bmi_chart_pointer_background)
        val blankSpaceAfter = vitalsCardView.findViewById<View>(R.id.bmi_chart_pointer_background2)

        val pointerParams = blankSpace.layoutParams as LinearLayout.LayoutParams
        val afterParams = blankSpaceAfter.layoutParams as LinearLayout.LayoutParams

        pointerParams.weight = weight
        afterParams.weight = 1 - weight

        blankSpace.layoutParams = pointerParams
        blankSpaceAfter.layoutParams = afterParams

    }

}

