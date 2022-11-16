package edu.upc.blopup.scale.showScaleMeasurement

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import edu.upc.blopup.getParcelable
import edu.upc.blopup.scale.readScaleMeasurement.EXTRAS_MEASUREMENT
import edu.upc.blopup.scale.readScaleMeasurement.WeightMeasurement
import org.openmrs.mobile.R
import org.openmrs.mobile.databinding.ActivityShowScaleBinding

class ShowScaleMeasurementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityShowScaleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.toolbarInclude.toolbar
        toolbar.title = getString(R.string.toolbar_show_measurement_title)
        toolbar.findViewById<ImageView>(R.id.bluetooth_icon)
            .setImageResource(R.drawable.ic_bluetooth_is_connected)
        setSupportActionBar(toolbar)

        val weightMeasurement = intent.getParcelable<WeightMeasurement>(EXTRAS_MEASUREMENT)!!

        with(binding) {
            weightMeasurementResult.text = weightMeasurement.weight.toString()

            sendWeightButton.setOnClickListener {
                val result = Intent().apply {
                    putExtra("weight", weightMeasurement.weight)
                }
                setResult(RESULT_OK, result)
                finish()
            }
        }
    }
}