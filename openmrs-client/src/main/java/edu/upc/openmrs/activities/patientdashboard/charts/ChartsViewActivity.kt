package edu.upc.openmrs.activities.patientdashboard.charts

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import edu.upc.R
import edu.upc.blopup.toggles.check
import edu.upc.blopup.toggles.treatmentChartToggle
import edu.upc.databinding.ActivityChartsViewBinding
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.openmrs.utilities.makeGone
import edu.upc.openmrs.utilities.makeVisible
import edu.upc.sdk.library.models.MedicationType
import edu.upc.sdk.utilities.ApplicationConstants
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@RequiresApi(Build.VERSION_CODES.O)
class ChartsViewActivity : ACBaseActivity(), OnChartGestureListener, OnChartValueSelectedListener {

    private val viewModel: ChartsViewViewModel by viewModels()

    private lateinit var bloodPressureChart: LineChart
    private lateinit var treatmentsChart: LineChart
    private val maxValuesInView = 3f

    private lateinit var expandableListView: ExpandableListView
    private lateinit var expandableListAdapter: TreatmentsListExpandableListAdapter
    private lateinit var expandableListTitle: List<String>
    private lateinit var expandableListDetail: Map<String, List<TreatmentAdherence>>

    private val maxBloodPressureValueShown = 200f
    private val minBloodPressureValueShown = 40f

    private val patientLocalDbId by lazy {
        this.intent.getBundleExtra(ApplicationConstants.BUNDLE)!!.getInt(PATIENT_ID)
    }

    companion object {
        const val BLOOD_PRESSURE = "bloodPressure"
        const val PATIENT_ID = "patientId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityChartsViewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setToolbar()

        bloodPressureChart = mBinding.bloodPressureChart
        treatmentsChart = mBinding.treatmentsChart

        val bloodPressureData = getBloodPressureValues()

        treatmentChartToggle.check({
            bloodPressureChart = mBinding.bloodPressureChartWithMargin
            bloodPressureChart.makeVisible()
            mBinding.bloodPressureChart.makeGone()
            treatmentsChart.makeVisible()
            mBinding.expandableListView.makeVisible()
        })

        setBloodPressureData(bloodPressureChart, bloodPressureData)
        setTreatmentsData(treatmentsChart, getTreatmentValues(bloodPressureData))
        setChartMaxAndMinimumLimitLines(bloodPressureChart)
        applyCommonChartStyles(bloodPressureChart)
        applyCommonChartStyles(treatmentsChart)
        applyBloodPressureChartStyles(bloodPressureChart)
        applyTreatmentsChartStyles(treatmentsChart)

        expandableListView = mBinding.expandableListView

        expandableListView.setOnGroupCollapseListener { groupPosition ->
            Toast.makeText(
                applicationContext,
                expandableListTitle[groupPosition].toString() + " List Collapsed.",
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.treatments.observe(this) { treatments ->
            treatments.onSuccess {
                expandableListDetail = it
                expandableListTitle = expandableListDetail.map { it.key }
                expandableListAdapter =
                    TreatmentsListExpandableListAdapter(this.layoutInflater, expandableListTitle, expandableListDetail)
                expandableListView.setAdapter(expandableListAdapter)
                expandableListView.setOnGroupExpandListener { groupPosition ->
                    Toast.makeText(
                        applicationContext,
                        expandableListTitle[groupPosition].toString() + " List Expanded.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        lifecycleScope.launch { viewModel.fetchTreatments(patientLocalDbId) }
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

    private fun getBloodPressureValues(): List<BloodPressureChartValue> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

        val mBundle = this.intent.getBundleExtra(ApplicationConstants.BUNDLE)

        val bloodPressureData =
            mBundle!!.getSerializable(BLOOD_PRESSURE) as HashMap<String, Pair<Float, Float>>
        val chartValues = bloodPressureData.map {
            BloodPressureChartValue(
                LocalDate.parse(it.key, formatter),
                it.value.first,
                it.value.second
            )
        }
        // We sort the values by date and remove duplicates keeping the last visit of the day
        return chartValues.sortedBy { it.date }.distinctBy { it.date }
    }

    private fun getTreatmentValues(bloodPressureValues: List<BloodPressureChartValue>): List<TreatmentChartValue> {
        return bloodPressureValues.map {
            TreatmentChartValue(
                it.date,
                getAdherenceForDate(it.date)
            )
        }
    }

    private fun getAdherenceForDate(date: LocalDate): FollowTreatments {
        // TODO: Implement logic to get the adherence for a given date
        return FollowTreatments.values().toList().shuffled().first()
    }

    private fun getPillIconForAdherence(adherence: FollowTreatments) = when (adherence) {
        FollowTreatments.NO_TREATMENTS -> ContextCompat.getDrawable(
            applicationContext,
            R.drawable.ic_treatment_pill
        )!!.apply { setTint(Color.GRAY) }

        FollowTreatments.FOLLOW_ALL -> ContextCompat.getDrawable(
            applicationContext,
            R.drawable.ic_treatment_pill
        )!!.apply {
            setTint(getColorFromResource(R.color.bp_normal))
        }

        FollowTreatments.FOLLOW_SOME -> ContextCompat.getDrawable(
            applicationContext,
            R.drawable.ic_treatment_pill
        )!!.apply {
            setTint(getColorFromResource(R.color.bp_ht_stage_I))
        }

        FollowTreatments.FOLLOW_NONE -> ContextCompat.getDrawable(
            applicationContext,
            R.drawable.ic_treatment_pill
        )!!.apply {
            setTint(getColorFromResource(R.color.bp_ht_stage_II_C))
        }
    }

    private fun getColorFromResource(resource: Int) =
        ContextCompat.getColor(applicationContext, resource)

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
        // Listener to sync the two charts scroll
        chart.onChartGestureListener = this
        chart.setOnChartValueSelectedListener(this)
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

    private fun generateDashedLineAt(value: Float, color: Int) =
        LimitLine(value).apply { lineColor = color; lineWidth = 2f; enableDashedLine(10f, 10f, 0f) }

    private fun applyCommonChartStyles(mChart: LineChart) {
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
        treatmentChartToggle.check({
            mChart.setTouchEnabled(false)
        }, {
            mChart.setTouchEnabled(true)
        })
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

    private fun applyTreatmentsChartStyles(mChart: LineChart) {
        mChart.setTouchEnabled(true)
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

    private fun isSystolicHigh(value: Float) = value >= 130F
    private fun isDiastolicHigh(value: Float) = value >= 80F
    private fun getSystolicPointIcon(value: Float) = getPointIcon(isSystolicHigh(value))
    private fun getDiastolicPointIcon(value: Float) = getPointIcon(isDiastolicHigh(value))
    private fun getPointIcon(isAboveMax: Boolean): Drawable {
        return if (isAboveMax) {
            ContextCompat.getDrawable(applicationContext, R.drawable.red)!!
        } else {
            ContextCompat.getDrawable(applicationContext, R.drawable.green)!!
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
        // TODO: Option to sync pinch zoom
        Log.i("Gesture", "SCALE X: $scaleX Y: $scaleY")
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        // TODO: Option to react to clicks
        Log.i("Entry selected", e.toString())
    }

    override fun onNothingSelected() {
    }
}

data class BloodPressureChartValue(val date: LocalDate, val systolic: Float, val diastolic: Float)
data class TreatmentChartValue(val date: LocalDate, val followTreatments: FollowTreatments)

enum class FollowTreatments {
    NO_TREATMENTS, FOLLOW_ALL, FOLLOW_SOME, FOLLOW_NONE
}
data class TreatmentAdherence(
    val name: String,
    var medicationType: Set<MedicationType>,
    val adherence: Boolean,
    val date: String
)

fun TreatmentAdherence.medicationTypeToString(context: Context): String {
    return medicationType.joinToString(separator = " • ") { context.getString(it.label) }
}

fun TreatmentAdherence.icon(): Int {
    return if (adherence) R.drawable.ic_tick else R.drawable.ic_cross
}
