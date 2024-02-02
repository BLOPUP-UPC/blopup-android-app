package edu.upc.openmrs.activities.patientdashboard.charts

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import edu.upc.R
import edu.upc.databinding.ActivityChartsViewBinding
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.sdk.utilities.ApplicationConstants
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
class ChartsViewActivity : ACBaseActivity(), OnChartGestureListener {

    private lateinit var mBinding: ActivityChartsViewBinding
    private lateinit var bloodPressureChart: LineChart
    private lateinit var treatmentsChart: LineChart

    companion object {
        const val BLOOD_PRESSURE = "bloodPressure"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityChartsViewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setToolbar()

        bloodPressureChart = mBinding.bloodPressureChart
        treatmentsChart = mBinding.treatmentsChart

        val bloodPressureData = getBloodPressureValues()

        setBloodPressureData(bloodPressureChart, bloodPressureData)
        setTreatmentsData(treatmentsChart, getTreatmentValues(bloodPressureData))
        setChartMaxAndMinimumLimitLines(bloodPressureChart)
        setChartFormat(bloodPressureChart)
        setChartFormatTreatments(treatmentsChart)
    }

    private fun setToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        if (toolbar != null) {
            toolbar.title = getString(R.string.charts_view_toolbar_title)
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setBloodPressureData(
        chart: LineChart,
        bloodPressureData: List<BloodPressureChartValue>
    ) {
        val allDatesValues = bloodPressureData.map { it.date.toString() }

        // Create entries where x is 0 to n-1 and y is the value
        // Then we show the date in the x axis based on the order
        val systolicEntries = bloodPressureData.mapIndexed { index, value -> Entry(index.toFloat(), value.systolic, getSystolicPointIcon(value.systolic)) }
        val diastolicEntries = bloodPressureData.mapIndexed { index, value -> Entry(index.toFloat(), value.diastolic, getDiastolicPointIcon(value.diastolic)) }

        val dataSetSystolic = LineDataSet(systolicEntries, "")
        val dataSetDiastolic = LineDataSet(diastolicEntries, "")

        //makes the line between data points transparent
        dataSetDiastolic.color = Color.TRANSPARENT
        dataSetSystolic.color = Color.TRANSPARENT

        // Size of the vale text (systolic or diastolic)
        dataSetDiastolic.valueTextSize = 12f
        dataSetSystolic.valueTextSize = 12f

        // We set the circle radius to 8 to put the text value above the actual icon
        // This size must be adjusted if the icon size changes
        dataSetSystolic.circleRadius = 8F
        dataSetDiastolic.circleRadius = 8F

        // We remove the native circle as we are showing a custom icon (red or green point)
        dataSetDiastolic.setCircleColor(Color.TRANSPARENT)
        dataSetSystolic.setCircleColor(Color.TRANSPARENT)

        val dataSets = arrayListOf(dataSetSystolic, dataSetDiastolic)
        val lineData = LineData(dataSets as List<ILineDataSet>?)

        chart.data = lineData
        // Format the values in the x axis to show the date instead of 0 to n-1
        chart.xAxis.valueFormatter = MyValueFormatter(allDatesValues)
    }

    private fun getBloodPressureValues(): List<BloodPressureChartValue> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

        val mBundle = this.intent.getBundleExtra(ApplicationConstants.BUNDLE)

        val bloodPressureData =
            mBundle!!.getSerializable(BLOOD_PRESSURE) as HashMap<String, Pair<Float, Float>>
        val chartValues = bloodPressureData.map { BloodPressureChartValue(LocalDate.parse(it.key, formatter), it.value.first, it.value.second) }
        return chartValues.sortedBy { it.date }.distinctBy { it.date }
    }

    private fun getTreatmentValues(bloodPressureValues: List<BloodPressureChartValue>): List<TreatmentChartValue> {
        return bloodPressureValues.map { TreatmentChartValue(it.date, getAdherenceForDate(it.date)) }
    }

    private fun getAdherenceForDate(date: LocalDate): FollowTreatments {
        // TODO: Implement logic to get the adherence for a given date
        return FollowTreatments.values().toList().shuffled().first()
    }

    data class BloodPressureChartValue(val date: LocalDate, val systolic: Float, val diastolic: Float)
    data class TreatmentChartValue(val date: LocalDate, val followTreatments: FollowTreatments)

    // TODO: Put the exact color values
    private fun getPillIconForAdherence(adherence: FollowTreatments) = when (adherence) {
        FollowTreatments.NO_TREATMENTS -> ContextCompat.getDrawable(applicationContext, R.drawable.ic_treatment_pill)!!.apply { setTint(Color.GRAY) }
        FollowTreatments.FOLLOW_ALL -> ContextCompat.getDrawable(applicationContext, R.drawable.ic_treatment_pill)!!.apply { setTint(Color.GREEN) }
        FollowTreatments.FOLLOW_SOME -> ContextCompat.getDrawable(applicationContext, R.drawable.ic_treatment_pill)!!.apply { setTint(Color.YELLOW) }
        FollowTreatments.FOLLOW_NONE -> ContextCompat.getDrawable(applicationContext, R.drawable.ic_treatment_pill)!!.apply { setTint(Color.RED) }
    }

    private fun setTreatmentsData(chart: LineChart, treatmentsData : List<TreatmentChartValue>) {
        val allDatesValues = treatmentsData.map { it.date.toString() }
        val treatmentsEntries = treatmentsData.mapIndexed { index, value -> Entry(index.toFloat(), 230f, getPillIconForAdherence(value.followTreatments)) }
        val dataSetTreatments = LineDataSet(treatmentsEntries, "")

        // Hide the native circle and value text, we only show the pill icon
        dataSetTreatments.valueTextSize = 0f
        dataSetTreatments.color = Color.TRANSPARENT
        dataSetTreatments.setCircleColor(Color.TRANSPARENT)

        chart.data = LineData(listOf(dataSetTreatments))
        chart.xAxis.valueFormatter = MyValueFormatter(allDatesValues)
        // Listener to sync the two charts scroll
        chart.onChartGestureListener = this
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

    private fun generateDashedLineAt(value: Float, color: Int) = LimitLine(value).apply { lineColor = color; lineWidth = 2f; enableDashedLine(10f, 10f, 0f) }

    private fun setChartFormat(mChart: LineChart) {
        mChart.description.isEnabled = false
        mChart.setTouchEnabled(false)
        // to make it scrollable
        mChart.setVisibleXRangeMaximum(3f)
        // to remove values in right side of screen
        mChart.axisRight.isEnabled = false
        //to display one data per date
        mChart.xAxis.granularity = 1F
        //to display the dates only in the bottom and not in the top as well
        mChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        mChart.xAxis.axisLineWidth = 2f
        mChart.xAxis.axisLineColor = Color.BLACK
        mChart.axisLeft.axisLineWidth = 2f
        mChart.axisLeft.axisLineColor = Color.BLACK
        //max and min values in the axis with the values
        mChart.axisLeft.axisMaximum = 200f
        mChart.axisLeft.axisMinimum = 40f
        mChart.axisLeft.setDrawLimitLinesBehindData(true)
        //to add some space before the first and last values on the chart
        mChart.xAxis.axisMinimum = -0.1F
        mChart.xAxis.axisMaximum = mChart.xAxis.axisMaximum + 0.1F
        //to open the chart focused on the most recent value
        mChart.moveViewToX(mChart.xAxis.axisMaximum)
        mChart.legend.isEnabled = false
    }

    private fun setChartFormatTreatments(mChart: LineChart) {
        mChart.description.isEnabled = false
        mChart.setTouchEnabled(true)
        // to make it scrollable
        mChart.setVisibleXRangeMaximum(3f)
        // to remove values in right side of screen
        mChart.axisRight.isEnabled = false
        //to display one data per date
        mChart.xAxis.disableGridDashedLine()
        mChart.xAxis.disableAxisLineDashedLine()
        //to display the dates only in the bottom and not in the top as well
        mChart.axisLeft.axisMaximum = 240f
        mChart.axisLeft.axisMinimum = 40f
        // Remove x grid lines
        mChart.axisLeft.gridColor = Color.TRANSPARENT
        // Remove axis line
        mChart.axisLeft.axisLineColor = Color.TRANSPARENT
        // Remove axis labels
        mChart.axisLeft.textColor = Color.TRANSPARENT
        mChart.xAxis.axisLineColor = Color.TRANSPARENT
        mChart.xAxis.textColor = Color.TRANSPARENT
        mChart.xAxis.gridColor = Color.TRANSPARENT
        //to add some space before the first and last values on the chart
        mChart.xAxis.axisMinimum = -0.1F
        mChart.xAxis.axisMaximum = mChart.xAxis.axisMaximum + 0.1F
        //to open the chart focused on the most recent value
        mChart.moveViewToX(mChart.xAxis.axisMaximum)
        mChart.legend.isEnabled = false
        //to display one data per date
        mChart.xAxis.granularity = 1F
    }

    private fun isSystolicHigh(value: Float) = value >= 130F
    private fun isDiastolicHigh(value: Float) = value >= 80F
    private fun getSystolicPointIcon(value: Float) = getPointIcon(isSystolicHigh(value))
    private fun getDiastolicPointIcon(value: Float) = getPointIcon(isDiastolicHigh(value))
    private fun getPointIcon(isAboveMax: Boolean): Drawable {
        if (isAboveMax) {
            return ContextCompat.getDrawable(applicationContext, R.drawable.red)!!
        } else {
            return ContextCompat.getDrawable(applicationContext, R.drawable.green)!!
        }
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
        bloodPressureChart.moveViewToX(treatmentsChart.lowestVisibleX)
    }

    override fun onChartGestureStart(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        // Explicit blank, not needed
    }

    override fun onChartGestureEnd(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {
        // Explicit blank, not needed
    }

    override fun onChartLongPressed(me: MotionEvent?) {
        // Explicit blank, not needed
    }

    override fun onChartDoubleTapped(me: MotionEvent?) {
        // Explicit blank, not needed
    }

    override fun onChartSingleTapped(me: MotionEvent?) {
        // Explicit blank, not needed
    }

    override fun onChartFling(
        me1: MotionEvent?,
        me2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ) {
        // Explicit blank, not needed
    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        // Explicit blank, not needed
    }
}

enum class FollowTreatments {
    NO_TREATMENTS, FOLLOW_ALL, FOLLOW_SOME, FOLLOW_NONE
}