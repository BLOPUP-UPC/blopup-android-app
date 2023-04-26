package edu.upc.openmrs.activities.patientdashboard.charts

import android.graphics.Color
import android.os.Build
import android.os.Bundle
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
import edu.upc.R
import edu.upc.databinding.ActivityChartsViewBinding
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.sdk.utilities.ApplicationConstants
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class ChartsViewActivity : ACBaseActivity() {

    private lateinit var mBinding: ActivityChartsViewBinding
    private lateinit var mChart: LineChart

    companion object {
        const val SYSTOLIC = "systolic"
        const val DIASTOLIC = "diastolic"
        const val BLOOD_PRESSURE = "bloodPressure"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityChartsViewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setToolbar()

        mChart = mBinding.linechart

        setChartData()
        setChartMaxAndMinimumLimitLines()
        setChartFormat()
    }

    private fun setToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        if (toolbar != null) {
            toolbar.title = getString(R.string.charts_view_toolbar_title)
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun setChartData() {
        val mBundle = this.intent.getBundleExtra(ApplicationConstants.BUNDLE)

        val bloodPressureData =
            mBundle!!.getSerializable(BLOOD_PRESSURE) as HashMap<String, Pair<Float, Float>>

        val systolicData = ArrayList<Float>()
        val diastolicData = ArrayList<Float>()
        val datesData = ArrayList<String>()

        for (key in bloodPressureData.keys) {
            systolicData.add((bloodPressureData[key]!!.first))
            diastolicData.add((bloodPressureData[key]!!.second))
            datesData.add(key)
        }

        val systolicEntries = setEntries(systolicData)
        val diastolicEntries = setEntries(diastolicData)

        val dataSetSystolic = LineDataSet(setColorIconsToEntries(systolicEntries, SYSTOLIC), "")
        val dataSetDiastolic = LineDataSet(setColorIconsToEntries(diastolicEntries, DIASTOLIC), "")

        //makes the line between data points transparent
        dataSetDiastolic.color = Color.TRANSPARENT
        dataSetSystolic.color = Color.TRANSPARENT

        dataSetDiastolic.valueTextSize = 12f
        dataSetSystolic.valueTextSize = 12f

        dataSetSystolic.circleRadius = 8F
        dataSetDiastolic.circleRadius = 8F

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(dataSetSystolic)
        dataSets.add(dataSetDiastolic)

        val lineData = LineData(dataSets)

        mChart.data = lineData
        mChart.xAxis.valueFormatter = MyValueFormatter(datesData)
    }

    private fun setEntries(valueDataArray: ArrayList<Float>): ArrayList<Entry> {
        val values = ArrayList<Entry>()
        var entryNumber = 0f
        for (value in valueDataArray) {
            val entry = Entry(entryNumber, value)
            values.add(entry)
            entryNumber++
        }
        return values
    }

    private fun setChartMaxAndMinimumLimitLines() {
        val minLimitSystolic = LimitLine(80f)
        applyGreenLine(minLimitSystolic)
        applyLimitLineStyle(minLimitSystolic)

        val maxLimitSystolic = LimitLine(90f)
        applyRedLine(maxLimitSystolic)
        applyLimitLineStyle(maxLimitSystolic)

        val minLimitDiastolic = LimitLine(130f)
        applyGreenLine(minLimitDiastolic)
        applyLimitLineStyle(minLimitDiastolic)

        val maxLimitDiastolic = LimitLine(140f)
        applyRedLine(maxLimitDiastolic)
        applyLimitLineStyle(maxLimitDiastolic)

        mChart.axisLeft.addLimitLine(minLimitSystolic)
        mChart.axisLeft.addLimitLine(maxLimitSystolic)
        mChart.axisLeft.addLimitLine(minLimitDiastolic)
        mChart.axisLeft.addLimitLine(maxLimitDiastolic)
    }

    private fun applyRedLine(upperLimitDiastolic: LimitLine) {
        upperLimitDiastolic.lineColor = Color.RED
    }

    private fun applyGreenLine(lowerLimitDiastolic: LimitLine) {
        lowerLimitDiastolic.lineColor = Color.GREEN
    }

    private fun applyLimitLineStyle(limitLine: LimitLine) {
        limitLine.lineWidth = 2f
        limitLine.enableDashedLine(10f, 10f, 0f)
    }

    private fun setChartFormat() {
        mChart.description.isEnabled = false
        mChart.setTouchEnabled(true)
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
    }

    private fun setColorIconsToEntries(entries: List<Entry>, type: String): List<Entry> {
        entries.forEach {
            if (type == SYSTOLIC && isSystolicHigh(it)) {
                it.icon = ContextCompat.getDrawable(applicationContext, R.drawable.red)
            } else if (type == DIASTOLIC && isDiastolicHigh(it)) {
                it.icon = ContextCompat.getDrawable(applicationContext, R.drawable.red)
            } else {
                it.icon = ContextCompat.getDrawable(applicationContext, R.drawable.green)
            }
        }
        return entries
    }
    private fun isSystolicHigh(it: Entry) = it.y >= 130F
    private fun isDiastolicHigh(it: Entry) = it.y >= 80F
}
