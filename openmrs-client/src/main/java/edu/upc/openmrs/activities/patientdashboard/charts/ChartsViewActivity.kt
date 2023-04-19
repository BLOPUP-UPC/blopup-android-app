package edu.upc.openmrs.activities.patientdashboard.charts

import android.annotation.SuppressLint
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
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
class ChartsViewActivity : ACBaseActivity() {

    private lateinit var mBinding: ActivityChartsViewBinding
    private lateinit var mChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityChartsViewBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setToolbar()

        mChart = mBinding.linechart

        setChartData()
        setChartLimitLines()
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

    @SuppressLint("SimpleDateFormat")
    private fun setChartData() {
        val mBundle = this.intent.getBundleExtra(ApplicationConstants.BUNDLE)

        val systolicData = mBundle!!.getString("systolic")
        val diastolicData = mBundle.getString("diastolic")
        val datesData = mBundle.getString("dates")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val outputFormat = SimpleDateFormat("yyyy/MM/dd")
        val dates = datesData!!.replace("[", "")
            .replace("]", "")
            .split(", ")
            .map { dateFormat.parse(it).toInstant().atZone(ZoneId.systemDefault()).toLocalDate() }
            .map { outputFormat.format(Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant())) }
            .toList()

        mChart.xAxis.valueFormatter = MyValueFormatter(dates)
        //to display one data per date
        mChart.xAxis.granularity = 1F

        val systolicDataString = systolicData?.replace("[^\\d.,]".toRegex(), "")?.split(",")
        val systolicDataArray = systolicDataString?.map { it.toDouble() }?.toDoubleArray()
        val systolicValues = ArrayList<Entry>()
        if (systolicDataArray != null) {
            var entryNumber =0f
            for(value in systolicDataArray){
                val entry = Entry(entryNumber, value.toFloat())
                systolicValues.add(entry)
                entryNumber++
            }
        }

        val diastolicDataString = diastolicData?.replace("[^\\d.,]".toRegex(), "")?.split(",")
        val diastolicDataArray = diastolicDataString?.map { it.toDouble() }?.toDoubleArray()
        val diastolicValues = ArrayList<Entry>()
        if (diastolicDataArray != null) {
            var entryNumber = 0f
            for(value in diastolicDataArray){
                val entry = Entry(entryNumber, value.toFloat())
                diastolicValues.add(entry)
                entryNumber++
            }
        }

        val dataSetSystolic = LineDataSet(assignEntryIconSystolic(systolicValues), "")
        val dataSetDiastolic = LineDataSet(assignEntryIconDiastolic(diastolicValues), "")

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
    }

    private fun setChartLimitLines() {
        //lines to reflect minimum and maximum values
        val lowerLimitDiastolic = LimitLine(130f)
        lowerLimitDiastolic.lineColor = Color.GREEN
        lowerLimitDiastolic.lineWidth = 2f
        lowerLimitDiastolic.enableDashedLine(10f, 10f, 0f)

        val upperLimitDiastolic = LimitLine(140f)
        upperLimitDiastolic.lineColor = Color.RED
        upperLimitDiastolic.lineWidth = 2f
        upperLimitDiastolic.enableDashedLine(10f, 10f, 0f)

        val lowerLimitSystolic = LimitLine(80f)
        lowerLimitSystolic.lineColor = Color.GREEN
        lowerLimitSystolic.lineWidth = 2f
        lowerLimitSystolic.enableDashedLine(10f, 10f, 0f)

        val upperLimitSystolic = LimitLine(90f)
        upperLimitSystolic.lineColor = Color.RED
        upperLimitSystolic.lineWidth = 2f
        upperLimitSystolic.enableDashedLine(10f, 10f, 0f)

        mChart.axisLeft.addLimitLine(lowerLimitDiastolic)
        mChart.axisLeft.addLimitLine(upperLimitDiastolic)
        mChart.axisLeft.addLimitLine(lowerLimitSystolic)
        mChart.axisLeft.addLimitLine(upperLimitSystolic)
    }

    private fun setChartFormat() {
        mChart.description.isEnabled = false
        mChart.setTouchEnabled(true)
        // to make it scrollable
        mChart.setVisibleXRangeMaximum(3f)
        mChart.axisLeft.setDrawLimitLinesBehindData(true)
        // to remove values in right side of screen
        mChart.axisRight.isEnabled = false
        mChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        mChart.axisLeft.axisMaximum = 200f
        mChart.axisLeft.axisMinimum = 40f
        mChart.xAxis.axisLineWidth = 2f
        mChart.xAxis.axisLineColor = Color.BLACK
        mChart.axisLeft.axisLineWidth = 2f
        mChart.axisLeft.axisLineColor = Color.BLACK
    }

    private fun assignEntryIconSystolic(entries: List<Entry>): List<Entry> {
        //depending on the value, each entry is assigned a red or green icon
        entries.forEach {
            if (it.y >= 130F) {
                it.icon = ContextCompat.getDrawable(applicationContext, R.drawable.red)
            } else {
                it.icon = ContextCompat.getDrawable(applicationContext, R.drawable.green)
            }
        }
        return entries
    }

    private fun assignEntryIconDiastolic(entries: List<Entry>): List<Entry> {
        entries.forEach {
            if (it.y >= 80F) {
                it.icon = ContextCompat.getDrawable(applicationContext, R.drawable.red)
            } else {
                it.icon = ContextCompat.getDrawable(applicationContext, R.drawable.green)
            }
        }
        return entries
    }
}