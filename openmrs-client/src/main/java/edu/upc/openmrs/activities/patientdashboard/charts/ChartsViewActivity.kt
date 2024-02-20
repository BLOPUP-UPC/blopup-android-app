package edu.upc.openmrs.activities.patientdashboard.charts

import android.os.Bundle
import android.view.MotionEvent
import android.widget.ExpandableListView
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
import edu.upc.databinding.ActivityChartsViewBinding
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.openmrs.utilities.makeVisible
import edu.upc.sdk.utilities.ApplicationConstants
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ChartsViewActivity : ACBaseActivity(), OnChartGestureListener, OnChartValueSelectedListener {

    private val viewModel: ChartsViewViewModel by viewModels()

    private lateinit var bloodPressureChartView: LineChart
    private lateinit var treatmentsChartView: LineChart
    private lateinit var bloodPressureChartPainter: BloodPressureChart

    private lateinit var expandableSidebarAdapter: TreatmentsListExpandableListAdapter
    private lateinit var expandableSidebarListView: ExpandableListView

    private val patientLocalDbId by lazy {
        this.intent.getBundleExtra(ApplicationConstants.BUNDLE)!!.getInt(PATIENT_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityChartsViewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setToolbar()
        setUpCharts(mBinding)
        setUpSidebarObserver(mBinding)

        lifecycleScope.launch { viewModel.fetchTreatments(patientLocalDbId) }
    }

    private fun setUpCharts(mBinding: ActivityChartsViewBinding) {
        bloodPressureChartView = mBinding.bloodPressureChart
        treatmentsChartView = mBinding.treatmentsChart
        bloodPressureChartPainter = BloodPressureChart(this)

        val bloodPressureData = getBloodPressureValues()

        bloodPressureChartPainter.paintBloodPressureChart(bloodPressureChartView, bloodPressureData)
        bloodPressureChartPainter.setListeners(treatmentsChartView, this, this)
    }

    private fun setUpSidebarObserver(mBinding: ActivityChartsViewBinding) {
        expandableSidebarListView = mBinding.expandableListView

        viewModel.treatments.observe(this) { treatments ->
            treatments.onSuccess { adherenceMap ->
                if (adherenceMap.isNotEmpty()) {
                    mBinding.treatmentsSideBar.makeVisible()
                    expandableSidebarAdapter = TreatmentsListExpandableListAdapter(
                        this.layoutInflater,
                        adherenceMap.map { it.key },
                        adherenceMap
                    )

                    mBinding.marginTop.makeVisible()
                    treatmentsChartView.makeVisible()
                    bloodPressureChartPainter.paintTreatmentsChart(
                        treatmentsChartView,
                        getBloodPressureValues(),
                        adherenceMap
                    )

                    expandableSidebarListView.setAdapter(expandableSidebarAdapter)
                }
            }
        }
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
        // Sync blood pressure and treatments charts horizontal scroll
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
        // Explicit blank, not needed
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e == null) return

        val dateSelected = getBloodPressureValues().map { it.date }[e.x.toInt()]
        val indexSelected = expandableSidebarAdapter.getTreatmentIdToExpand(dateSelected)

        if (indexSelected == -1) return

        if (expandableSidebarListView.isGroupExpanded(indexSelected)) {
            expandableSidebarListView.collapseGroup(indexSelected)
        } else {
            expandableSidebarListView.expandGroup(indexSelected)
        }
    }

    override fun onNothingSelected() {
        // Explicit blank, not needed
    }

    companion object {
        const val BLOOD_PRESSURE = "bloodPressure"
        const val PATIENT_ID = "patientId"
    }
}

