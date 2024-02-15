package edu.upc.openmrs.activities.patientdashboard.charts

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import edu.upc.R
import edu.upc.sdk.library.models.MedicationType
import java.time.LocalDate

class BloodPressureChart(private val context: Context) {

    private val maxValuesInView = 4f
    private val maxBloodPressureValueShown = 200f
    private val minBloodPressureValueShown = 40f

    fun paintBloodPressureChart(
        chartLayout: LineChart,
        bloodPressureData: List<BloodPressureChartValue>
    ) {
        setBloodPressureData(chartLayout, bloodPressureData)
        setChartMaxAndMinimumLimitLines(chartLayout)
        applyCommonChartStyles(chartLayout)
        applyBloodPressureChartStyles(chartLayout)
    }

    fun paintTreatmentsChart(
        chartLayout: LineChart,
        bloodPressureData: List<BloodPressureChartValue>,
        adherenceMap: Map<LocalDate, List<TreatmentAdherence>>
    ) {
        setTreatmentsData(chartLayout, getTreatmentValues(bloodPressureData, adherenceMap))
        applyCommonChartStyles(chartLayout)
        applyTreatmentsChartStyles(chartLayout)
    }

    private fun setBloodPressureData(
        chart: LineChart,
        bloodPressureData: List<BloodPressureChartValue>
    ) {
        val allDatesValues = bloodPressureData.map { it.date.toString() }

        // Create entries where x is 0 to n-1 and y is the value
        // Then we show the date in the x axis based on the order
        val systolicEntries = bloodPressureData.mapIndexed { index, value ->
            Entry(
                index.toFloat(),
                value.systolic,
                getSystolicPointIcon(value.systolic)
            )
        }
        val diastolicEntries = bloodPressureData.mapIndexed { index, value ->
            Entry(
                index.toFloat(),
                value.diastolic,
                getDiastolicPointIcon(value.diastolic)
            )
        }

        val systolicLine = LineDataSet(systolicEntries, "")
        val diastolicLine = LineDataSet(diastolicEntries, "")

        //makes the line between data points transparent
        diastolicLine.color = Color.TRANSPARENT
        systolicLine.color = Color.TRANSPARENT

        // Size of the vale text (systolic or diastolic)
        diastolicLine.valueTextSize = 12f
        systolicLine.valueTextSize = 12f

        // We set the circle radius to 8 to put the text value above the actual icon
        // This size must be adjusted if the icon size changes
        systolicLine.circleRadius = 8F
        diastolicLine.circleRadius = 8F

        // We remove the native circle as we are showing a custom icon (red or green point)
        diastolicLine.setCircleColor(Color.TRANSPARENT)
        systolicLine.setCircleColor(Color.TRANSPARENT)

        chart.data = LineData(listOf(systolicLine, diastolicLine))
        // Format the values in the x axis to show the date instead of 0 to n-1
        chart.xAxis.valueFormatter = MyValueFormatter(allDatesValues)
    }

    private fun setChartMaxAndMinimumLimitLines(chart: LineChart) {
        val minLimitSystolic = generateDashedLineAt(80f, Color.GREEN)
        val maxLimitSystolic = generateDashedLineAt(90f, Color.RED)

        val minLimitDiastolic = generateDashedLineAt(130f, Color.GREEN)
        val maxLimitDiastolic = generateDashedLineAt(140f, Color.RED)

        chart.axisLeft.addLimitLine(minLimitSystolic)
        chart.axisLeft.addLimitLine(maxLimitSystolic)
        chart.axisLeft.addLimitLine(minLimitDiastolic)
        chart.axisLeft.addLimitLine(maxLimitDiastolic)
    }

    private fun applyCommonChartStyles(mChart: LineChart) {
        mChart.setTouchEnabled(true)
        mChart.description.isEnabled = false
        // to make it scrollable
        mChart.setVisibleXRangeMaximum(maxValuesInView)
        // to remove values in right side of screen
        mChart.axisRight.isEnabled = false
        //to open the chart focused on the most recent values
        mChart.moveViewToX(mChart.xAxis.axisMaximum)
        mChart.legend.isEnabled = false
        //to add some space before the first and last values on the chart
        mChart.xAxis.axisMinimum = -0.1F
        mChart.xAxis.axisMaximum = mChart.xAxis.axisMaximum + 0.1F

        // To show only
        mChart.xAxis.granularity = 1f

        //min values in the axis with the values
        mChart.axisLeft.axisMinimum = minBloodPressureValueShown
        mChart.isDoubleTapToZoomEnabled = false
        mChart.isScaleYEnabled = false
        // TODO: Enable if we are able to sync pinch zoom
        mChart.isScaleXEnabled = false
    }

    private fun applyBloodPressureChartStyles(mChart: LineChart) {
        //to display the dates only in the bottom and not in the top as well
        mChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        mChart.xAxis.axisLineWidth = 2f
        mChart.xAxis.axisLineColor = Color.BLACK
        mChart.axisLeft.axisLineWidth = 2f
        mChart.axisLeft.axisLineColor = Color.BLACK
        //max and min values in the axis with the values
        mChart.axisLeft.axisMaximum = maxBloodPressureValueShown

        // Remove horizontal grid lines
        mChart.axisLeft.gridColor = Color.TRANSPARENT
    }

    private fun getTreatmentValues(
        bloodPressureValues: List<BloodPressureChartValue>,
        adherenceMap: Map<LocalDate, List<TreatmentAdherence>>
    ): List<TreatmentChartValue> {
        return bloodPressureValues.map {
            TreatmentChartValue(
                it.date,
                getAdherenceForDate(it.date, adherenceMap)
            )
        }
    }

    private fun getAdherenceForDate(date: LocalDate, adherenceMap: Map<LocalDate, List<TreatmentAdherence>>): FollowTreatments {
        return adherenceMap[date]?.followTreatments() ?: FollowTreatments.NO_INFO
    }

    private fun setTreatmentsData(chart: LineChart, treatmentsData: List<TreatmentChartValue>) {
        val allDatesValues = treatmentsData.map { it.date.toString() }
        val treatmentsEntries = treatmentsData.mapIndexed { index, value ->
            Entry(
                index.toFloat(),
                230f,
                getPillIconForAdherence(value.followTreatments)
            )
        }
        val dataSetTreatments = LineDataSet(treatmentsEntries, "")

        // Hide the native circle and value text, we only show the pill icon
        dataSetTreatments.valueTextSize = 0f
        dataSetTreatments.color = Color.TRANSPARENT
        dataSetTreatments.setCircleColor(Color.TRANSPARENT)

        chart.data = LineData(listOf(dataSetTreatments))
        chart.xAxis.valueFormatter = MyValueFormatter(allDatesValues)
    }

    private fun getPillIconForAdherence(adherence: FollowTreatments) = when (adherence) {
        FollowTreatments.NO_INFO -> ContextCompat.getDrawable(
            context,
            R.drawable.ic_treatment_pill
        )!!.apply { setTint(Color.TRANSPARENT) }

        FollowTreatments.FOLLOW_ALL -> ContextCompat.getDrawable(
            context,
            R.drawable.ic_treatment_pill
        )!!.apply {
            setTint(getColorFromResource(R.color.bp_normal))
        }

        FollowTreatments.FOLLOW_SOME -> ContextCompat.getDrawable(
            context,
            R.drawable.ic_treatment_pill
        )!!.apply {
            setTint(getColorFromResource(R.color.bp_ht_stage_I))
        }

        FollowTreatments.FOLLOW_NONE -> ContextCompat.getDrawable(
            context,
            R.drawable.ic_treatment_pill
        )!!.apply {
            setTint(getColorFromResource(R.color.bp_ht_stage_II_C))
        }
    }

    private fun applyTreatmentsChartStyles(mChart: LineChart) {
        mChart.xAxis.axisLineColor = Color.TRANSPARENT
        mChart.xAxis.textColor = Color.TRANSPARENT
        // Hide vertical grid lines
        mChart.xAxis.gridColor = Color.TRANSPARENT

        // We want to show the pill above any possible value
        mChart.axisLeft.axisMaximum = maxBloodPressureValueShown + 40f
        // Remove horizontal grid lines
        mChart.axisLeft.gridColor = Color.TRANSPARENT
        // Hide left axis
        mChart.axisLeft.textColor = Color.TRANSPARENT
        mChart.axisLeft.axisLineColor = Color.TRANSPARENT
    }

    private fun getColorFromResource(resource: Int) =
        ContextCompat.getColor(context, resource)


    private fun generateDashedLineAt(value: Float, color: Int) =
        LimitLine(value).apply { lineColor = color; lineWidth = 2f; enableDashedLine(10f, 10f, 0f) }


    private fun isSystolicHigh(value: Float) = value >= 130F
    private fun isDiastolicHigh(value: Float) = value >= 80F
    private fun getSystolicPointIcon(value: Float) = getPointIcon(isSystolicHigh(value))
    private fun getDiastolicPointIcon(value: Float) = getPointIcon(isDiastolicHigh(value))
    private fun getPointIcon(isAboveMax: Boolean): Drawable {
        return if (isAboveMax) {
            ContextCompat.getDrawable(context, R.drawable.red)!!
        } else {
            ContextCompat.getDrawable(context, R.drawable.green)!!
        }
    }

    fun setListeners(
        chart: LineChart,
        gestureListener: OnChartGestureListener,
        valueSelectedListener: OnChartValueSelectedListener
    ) {
        // Listener to sync the two charts scroll
        chart.onChartGestureListener = gestureListener
        chart.setOnChartValueSelectedListener(valueSelectedListener)
    }
}

data class BloodPressureChartValue(val date: LocalDate, val systolic: Float, val diastolic: Float)
data class TreatmentChartValue(val date: LocalDate, val followTreatments: FollowTreatments)

enum class FollowTreatments {
    NO_INFO, FOLLOW_ALL, FOLLOW_SOME, FOLLOW_NONE
}

data class TreatmentAdherence(
    val name: String,
    var medicationType: Set<MedicationType>,
    val adherence: Boolean,
    val date: LocalDate
)

fun TreatmentAdherence.medicationTypeToString(context: Context): String {
    return medicationType.joinToString(separator = " • ") { context.getString(it.label) }
}

fun TreatmentAdherence.icon(): Int {
    return if (adherence) R.drawable.ic_tick else R.drawable.ic_cross
}

fun List<TreatmentAdherence>.followTreatments(): FollowTreatments {
    val adherence = map { it.adherence }
    return when {
        adherence.all { it } -> FollowTreatments.FOLLOW_ALL
        adherence.none { it } -> FollowTreatments.FOLLOW_NONE
        else -> FollowTreatments.FOLLOW_SOME
    }
}