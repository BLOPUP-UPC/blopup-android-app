package edu.upc.blopup.showmeasurements

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.widget.Toolbar
import com.openmrs.android_sdk.utilities.ToastUtil
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.blopup.scale.readScaleMeasurement.EXTRAS_WEIGHT
import edu.upc.blopup.scale.readScaleMeasurement.ReadWeightActivity
import edu.upc.blopup.tensiometer.readTensiometerMeasurement.EXTRAS_DIASTOLIC
import edu.upc.blopup.tensiometer.readTensiometerMeasurement.EXTRAS_HEART_RATE
import edu.upc.blopup.tensiometer.readTensiometerMeasurement.EXTRAS_SYSTOLIC
import edu.upc.blopup.tensiometer.readTensiometerMeasurement.ReadTensiometerActivity
import edu.upc.databinding.ActivityVitalsFormBinding

@AndroidEntryPoint
class ShowMeasurementsActivity : edu.upc.openmrs.activities.ACBaseActivity() {

    private lateinit var mBinding: ActivityVitalsFormBinding
    private lateinit var mToolbar: Toolbar

    private val bluetoothTensiometerDataLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        val intent = result.data
        if (intent != null && result.resultCode == RESULT_OK) {
            val systolic = intent.extras!!.getInt(EXTRAS_SYSTOLIC)
            val diastolic = intent.extras!!.getInt(EXTRAS_DIASTOLIC)
            val pulse = intent.extras!!.getInt(EXTRAS_HEART_RATE)

            var systolicField = findViewById<View>(R.id.systolic) as EditText
            systolicField.setText(systolic.toString())
            systolicField.keyListener = null;
            var diastolicField = findViewById<View>(R.id.diastolic) as EditText
            diastolicField.setText(diastolic.toString())
            diastolicField.keyListener = null;
            var pulseField = findViewById<View>(R.id.pulse) as EditText
            pulseField.setText(pulse.toString())
            pulseField.keyListener = null;
        }
    }

    private val bluetoothScaleDataLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        val intent = result.data
        if (intent != null && result.resultCode == RESULT_OK) {
            val weight = intent.extras!!.getFloat(EXTRAS_WEIGHT)
            var weightField = findViewById<View>(R.id.weight) as EditText
            weightField.setText(weight.toString())
            weightField.keyListener = null;
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityVitalsFormBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setUpToolbar()

        mBinding.receiveTensiometerDataBtn.setOnClickListener {
            try {
                val input = Intent(this, ReadTensiometerActivity::class.java)
                bluetoothTensiometerDataLauncher.launch(input)
            } catch (ex: ActivityNotFoundException) {
                ToastUtil.error(
                    getString(R.string.receive_vitals_from_bluetooth_button_error_message),
                    Toast.LENGTH_LONG
                );
            }
        }

        mBinding.receiveWeightDataBtn.setOnClickListener {
            try {
                val input = Intent(this, ReadWeightActivity::class.java)
                bluetoothScaleDataLauncher.launch(input)
            } catch (ex: ActivityNotFoundException) {
                ToastUtil.error(
                    getString(R.string.receive_vitals_from_bluetooth_button_error_message),
                    Toast.LENGTH_LONG
                );
            }
        }
    }

    private fun setUpToolbar() {
        mToolbar = mBinding.toolbarVitals.toolbar
        mToolbar.title = getString(R.string.vitals_form_title)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
    }
}