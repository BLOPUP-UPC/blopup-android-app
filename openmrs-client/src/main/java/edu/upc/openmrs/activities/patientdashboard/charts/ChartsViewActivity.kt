package edu.upc.openmrs.activities.patientdashboard.charts

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
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


class ChartsViewActivity : ACBaseActivity(), OnChartGestureListener, OnChartValueSelectedListener {

    private val viewModel: ChartsViewViewModel by viewModels()

    private lateinit var bloodPressureChartView: LineChart
    private lateinit var treatmentsChartView: LineChart
    private lateinit var bloodPressureChartPainter: BloodPressureChart

    private lateinit var expandableListView: ExpandableListView
    private lateinit var expandableListAdapter: TreatmentsListExpandableListAdapter
    private lateinit var expandableListTitle: List<String>
    private lateinit var expandableListDetail: Map<String, List<TreatmentAdherence>>

    private val patientLocalDbId by lazy {
        this.intent.getBundleExtra(ApplicationConstants.BUNDLE)!!.getInt(PATIENT_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityChartsViewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setToolbar()

        bloodPressureChartView = mBinding.bloodPressureChart
        treatmentsChartView = mBinding.treatmentsChart
        bloodPressureChartPainter = BloodPressureChart(this)

        val bloodPressureData = getBloodPressureValues()

        treatmentChartToggle.check({
            bloodPressureChartView = mBinding.bloodPressureChartWithMargin
            bloodPressureChartView.makeVisible()
            mBinding.bloodPressureChart.makeGone()
            treatmentsChartView.makeVisible()
            mBinding.expandableListView.makeVisible()
        })

        bloodPressureChartPainter.paintBloodPressureChart(bloodPressureChartView, bloodPressureData)
        bloodPressureChartPainter.paintTreatmentsChart(treatmentsChartView, bloodPressureData)
        bloodPressureChartPainter.setListeners(treatmentsChartView, this, this)

        expandableListView = mBinding.expandableListView

        expandableListView.setOnGroupCollapseListener { groupPosition ->
            Toast.makeText(
                applicationContext,
                expandableListTitle[groupPosition].toString() + " List Collapsed.",
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.treatments.observe(this) { treatments ->
            treatments.onSuccess { adherenceMap ->
                if (adherenceMap.isNotEmpty()) {
                    mBinding.treatmentsSideBar.makeVisible()
                    expandableListDetail = adherenceMap
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

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
        bloodPressureChartView.moveViewToX(treatmentsChartView.lowestVisibleX)
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

    companion object {
        const val BLOOD_PRESSURE = "bloodPressure"
        const val PATIENT_ID = "patientId"
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
