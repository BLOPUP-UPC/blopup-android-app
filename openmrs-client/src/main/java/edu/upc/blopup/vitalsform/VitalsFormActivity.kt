package edu.upc.blopup.vitalsform

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.blopup.scale.readScaleMeasurement.EXTRAS_WEIGHT
import edu.upc.blopup.scale.readScaleMeasurement.ReadWeightActivity
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.*
import edu.upc.databinding.ActivityVitalsFormBinding
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.openmrs.utilities.observeOnce
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.utilities.ToastUtil

@AndroidEntryPoint
class VitalsFormActivity : ACBaseActivity() {

    private lateinit var mBinding: ActivityVitalsFormBinding
    private lateinit var mToolbar: Toolbar

    private val SYSTOLIC_FIELD_CONCEPT = "5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    private val DIASTOLIC_FIELD_CONCEPT = "5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    private val HEART_RATE_FIELD_CONCEPT = "5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    private val WEIGHT_FIELD_CONCEPT = "5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    private val HEIGHT_FIELD_CONCEPT = "5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"

    private val viewModel: VitalsFormViewModel by viewModels()

    private var systolic: String = ""
    private var diastolic: String = ""
    private var heartRate: String = ""
    private var weight: String = ""
    private var heightCm: String = ""
    private var receivedButtonModified: Boolean = false

    private val vitals: MutableList<Vital> = mutableListOf()

    private val bluetoothBloodPressureDataLauncher  = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        val intent = result.data
        if (intent != null && result.resultCode == RESULT_OK) {
            systolic = intent.extras!!.getInt(EXTRAS_SYSTOLIC).toString()
            diastolic = intent.extras!!.getInt(EXTRAS_DIASTOLIC).toString()
            heartRate = intent.extras!!.getInt(EXTRAS_HEART_RATE).toString()

            mBinding.systolic.setText(systolic)
            mBinding.diastolic.setText(diastolic)
            mBinding.pulse.setText(heartRate)
        }
    }

    private val bluetoothScaleDataLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        val intent = result.data
        if (intent != null && result.resultCode == RESULT_OK) {
            weight = intent.extras!!.getFloat(EXTRAS_WEIGHT).toString()

            mBinding.weight.setText(weight)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityVitalsFormBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setUpToolbar()
        mBinding.buttonToSentVitals.isEnabled = false
        mBinding.receiveBloodPressureDataBtn.setOnClickListener {
            try {
                startActivity(Intent(this, BloodPressureInstructionsActivity::class.java))
                val input = Intent(this, ReadBloodPressureActivity::class.java)
                bluetoothBloodPressureDataLauncher.launch(input)
                mBinding.buttonToSentVitals.isEnabled = true
                receivedButtonModified = true
            } catch (ex: ActivityNotFoundException) {
                ToastUtil.error(
                    getString(R.string.receive_vitals_from_bluetooth_button_error_message),
                    Toast.LENGTH_LONG
                )
            }
        }

        mBinding.receiveWeightDataBtn.setOnClickListener {
            try {
                val input = Intent(this, ReadWeightActivity::class.java)
                bluetoothScaleDataLauncher.launch(input)
                mBinding.buttonToSentVitals.isEnabled = true
                receivedButtonModified = true
            } catch (ex: ActivityNotFoundException) {
                ToastUtil.error(
                    getString(R.string.receive_vitals_from_bluetooth_button_error_message),
                    Toast.LENGTH_LONG
                )
            }
        }

        mBinding.buttonToSentVitals.setOnClickListener {
            heightCm = mBinding.height.text.toString()
            submitForm()
        }

        mBinding.height.addTextChangedListener {
            if (!receivedButtonModified) {
                mBinding.buttonToSentVitals.isEnabled = mBinding.height.text.toString().isNotEmpty()
            }
        }
    }

    private fun submitForm() {
        addVitalsToForm()
        viewModel.submitForm(vitals).observeOnce(this, Observer { result ->
            when (result) {
                ResultType.EncounterSubmissionSuccess -> {
                    ToastUtil.success(getString(R.string.form_submitted_successfully))
                    finish()
                }
                ResultType.EncounterSubmissionLocalSuccess -> {
                    ToastUtil.notify(getString(R.string.form_data_sync_is_off_message))
                    finish()
                }
                else -> ToastUtil.error(getString(R.string.form_data_submit_error))
            }
        })
    }

    private fun addVitalsToForm() {
        vitals.add(Vital(SYSTOLIC_FIELD_CONCEPT, systolic))
        vitals.add(Vital(DIASTOLIC_FIELD_CONCEPT, diastolic))
        vitals.add(Vital(HEART_RATE_FIELD_CONCEPT, heartRate))
        vitals.add(Vital(WEIGHT_FIELD_CONCEPT, weight))
        vitals.add(Vital(HEIGHT_FIELD_CONCEPT, heightCm))
    }


    private fun setUpToolbar() {
        mToolbar = mBinding.toolbarVitals.toolbar
        mToolbar.title = getString(R.string.vitals_form_title)
        setSupportActionBar(mToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
    }
}