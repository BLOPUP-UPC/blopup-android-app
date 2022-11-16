package edu.upc.blopup.tensiometer.showTensiometerMeasurement

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import edu.upc.blopup.getParcelable
import edu.upc.blopup.tensiometer.readTensiometerMeasurement.EXTRAS_MEASUREMENT
import edu.upc.blopup.tensiometer.readTensiometerMeasurement.Measurement
import org.openmrs.mobile.R
import org.openmrs.mobile.databinding.ActivityShowTensiometerBinding

class ShowTensiometerMeasurementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityShowTensiometerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbarInclude.toolbar
        toolbar.title = getString(R.string.toolbar_show_measurement_title)
        toolbar.findViewById<ImageView>(R.id.bluetooth_icon)
            .setImageResource(R.drawable.ic_bluetooth_is_connected)
        setSupportActionBar(toolbar)

        val measurement = intent.getParcelable<Measurement>(EXTRAS_MEASUREMENT)!!

        with(binding) {
            diastolicMeasurement.text = measurement.diastolic.toString()
            systolicMeasurement.text = measurement.systolic.toString()
            heartRate.text = measurement.heartRate.toString()

            sendMeasurementButton.setOnClickListener {
                val result = Intent().apply {
                    putExtra("systolic", measurement.systolic)
                    putExtra("diastolic", measurement.diastolic)
                    putExtra("heartRate", measurement.heartRate)
                }
                setResult(RESULT_OK, result)
                finish()
            }
        }
    }
}