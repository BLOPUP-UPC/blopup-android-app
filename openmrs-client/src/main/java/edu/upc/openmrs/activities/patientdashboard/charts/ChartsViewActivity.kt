package edu.upc.openmrs.activities.patientdashboard.charts

import android.os.Bundle
import android.view.MotionEvent
import android.widget.ExpandableListView
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import edu.upc.R
import edu.upc.blopup.ui.ResultUiState
import edu.upc.databinding.ActivityChartsViewBinding
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.openmrs.utilities.makeGone
import edu.upc.openmrs.utilities.makeVisible
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE
import edu.upc.sdk.utilities.ApplicationConstants.BundleKeys.PATIENT_UUID_BUNDLE
import edu.upc.sdk.utilities.DateUtils.toLocalDate
import edu.upc.sdk.utilities.ToastUtil
import java.time.LocalDate
import java.util.UUID

class ChartsViewActivity : ACBaseActivity(), OnChartGestureListener, OnChartValueSelectedListener {

    private val viewModel: ChartsViewViewModel by viewModels()

    private lateinit var bloodPressureChartView: LineChart
    private lateinit var treatmentsChartView: LineChart
    private lateinit var bloodPressureChartPainter: BloodPressureChart

    private lateinit var expandableSidebarAdapter: TreatmentsListExpandableListAdapter
    private lateinit var expandableSidebarListView: ExpandableListView

    private val patientLocalDbId by lazy {
        this.intent.getBundleExtra(ApplicationConstants.BUNDLE)!!.getInt(PATIENT_ID_BUNDLE)
    }

    private val patientUuid by lazy {
        this.intent.getBundleExtra(ApplicationConstants.BUNDLE)!!.getString(PATIENT_UUID_BUNDLE)
    }

    private var visitsDates: List<LocalDate>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBinding = ActivityChartsViewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setToolbar()
        setUpObservers(mBinding)

        viewModel.fetchVisitsWithTreatments(patientLocalDbId, UUID.fromString(patientUuid))
    }

    private fun setUpObservers(mBinding: ActivityChartsViewBinding) {
        viewModel.visitsWithTreatments.observe(this) { visitsWithTreatments ->
            when (visitsWithTreatments) {
                is ResultUiState.Loading -> {
                    mBinding.progressBar.makeVisible()
                    return@observe
                }

                is ResultUiState.Error -> {
                    mBinding.progressBar.makeGone()
                    ToastUtil.error(getString(R.string.visit_fetching_error))
                    return@observe
                }

                is ResultUiState.Success -> {
                    if (visitsWithTreatments.data.isEmpty()) {
                        mBinding.progressBar.makeGone()
                        mBinding.noDataTextView.makeVisible()
                        return@observe
                    }

                    this.visitsDates = visitsWithTreatments.data.map { it.visit.startDate.toLocalDate() }
                    showVisitsChart(mBinding, visitsWithTreatments.data)
                    showTreatmentsChartAndSidebar(mBinding, visitsWithTreatments.data)
                    mBinding.progressBar.makeGone()
                    mBinding.chartsView.makeVisible()
                }
            }
        }
    }

    private fun showVisitsChart(mBinding: ActivityChartsViewBinding, visits: List<VisitWithAdherence>) {
        bloodPressureChartView = mBinding.bloodPressureChart
        treatmentsChartView = mBinding.treatmentsChart
        bloodPressureChartPainter = BloodPressureChart(this)

        val bloodPressureData = visits
            .map {
                BloodPressureChartValue(
                    it.visit.startDate.toLocalDate(),
                    it.visit.bloodPressure.systolic.toFloat(),
                    it.visit.bloodPressure.diastolic.toFloat()
                )
        }

        bloodPressureChartPainter.paintBloodPressureChart(
            bloodPressureChartView,
            bloodPressureData
        )
        bloodPressureChartPainter.setListeners(treatmentsChartView, this, this)
    }

    private fun showTreatmentsChartAndSidebar(mBinding: ActivityChartsViewBinding, visits: List<VisitWithAdherence>) {
        expandableSidebarListView = mBinding.expandableListView

        val visitsWithAnyTreatment = visits.filter { it.adherence.isNotEmpty() }
        if (visitsWithAnyTreatment.isNotEmpty()) {
            mBinding.treatmentsSideBar.makeVisible()
            expandableSidebarAdapter = TreatmentsListExpandableListAdapter(
                this.layoutInflater,
                visitsWithAnyTreatment.map { it.visit.startDate.toLocalDate() },
                visitsWithAnyTreatment.associateBy({ it.visit.startDate.toLocalDate() }, { it.adherence })
            )

            mBinding.marginTop.makeVisible()
            treatmentsChartView.makeVisible()
            bloodPressureChartPainter.paintTreatmentsChart(
                treatmentsChartView,
                visits.map {
                    TreatmentChartValue(
                        it.visit.startDate.toLocalDate(),
                        it.adherence.followTreatments()
                    )
                }
            )

            expandableSidebarListView.setAdapter(expandableSidebarAdapter)
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

        visitsDates.let {
            val dateSelected = visitsDates?.get(e.x.toInt()) ?: return

            val indexSelected = expandableSidebarAdapter.getTreatmentIdToExpand(dateSelected)
            if (indexSelected == -1) return

            if (expandableSidebarListView.isGroupExpanded(indexSelected)) {
                expandableSidebarListView.collapseGroup(indexSelected)
            } else {
                expandableSidebarListView.expandGroup(indexSelected)
            }
        }
    }

    override fun onNothingSelected() {
        // Explicit blank, not needed
    }
}

