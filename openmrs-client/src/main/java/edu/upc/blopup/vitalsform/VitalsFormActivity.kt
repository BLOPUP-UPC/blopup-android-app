package edu.upc.blopup.vitalsform

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.EXTRAS_DIASTOLIC
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.EXTRAS_HEART_RATE
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.EXTRAS_SYSTOLIC
import edu.upc.blopup.bloodpressure.readBloodPressureMeasurement.ReadBloodPressureActivity
import edu.upc.blopup.scale.readScaleMeasurement.EXTRAS_WEIGHT
import edu.upc.blopup.scale.readScaleMeasurement.ReadScaleActivity
import edu.upc.databinding.ActivityVitalsFormBinding
import edu.upc.openmrs.activities.ACBaseActivity
import edu.upc.openmrs.activities.dialog.CustomFragmentDialog
import edu.upc.openmrs.bundle.CustomDialogBundle
import edu.upc.openmrs.utilities.observeOnce
import edu.upc.sdk.library.models.Result
import edu.upc.sdk.library.models.ResultType
import edu.upc.sdk.utilities.ApplicationConstants
import edu.upc.sdk.utilities.ApplicationConstants.vitalsConceptType.DIASTOLIC_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.vitalsConceptType.HEART_RATE_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.vitalsConceptType.HEIGHT_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.vitalsConceptType.SYSTOLIC_FIELD_CONCEPT
import edu.upc.sdk.utilities.ApplicationConstants.vitalsConceptType.WEIGHT_FIELD_CONCEPT
import edu.upc.sdk.utilities.ToastUtil
import kotlinx.android.synthetic.main.activity_vitals_form.buttonToSentVitals
import java.net.UnknownHostException

@AndroidEntryPoint
class VitalsFormActivity : ACBaseActivity() {

    lateinit var mBinding: ActivityVitalsFormBinding
    private lateinit var mToolbar: Toolbar

    private val viewModel: VitalsFormViewModel by viewModels()

    private var systolic: String = ""
    private var diastolic: String = ""
    private var heartRate: String = ""
    private var weight: String = ""
    private var heightCm: String = ""
    private var receivedButtonModified: Boolean = false

    private val vitals: MutableList<Vital> = mutableListOf()

    private val bluetoothBloodPressureDataLauncher = registerForActivityResult(
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

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setDialogToConfirmVitalsRemoval()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityVitalsFormBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setUpToolbar()

        getHeightValueFromPreviousVisits()

        mBinding.buttonToSentVitals.isEnabled = false
        mBinding.receiveBloodPressureDataBtn.setOnClickListener {
            try {
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
                val input = Intent(this, ReadScaleActivity::class.java)
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

        mBinding.height.addTextChangedListener {
            if (!receivedButtonModified) {
                mBinding.buttonToSentVitals.isEnabled = mBinding.height.text.toString().isNotEmpty()
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        askForPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (vitals.isNotEmpty()) {
            mBinding.buttonToSentVitals.isEnabled = true
        }

        updateLanguageFields()
    }

    fun sendVitals(view: View) {
        buttonToSentVitals.isEnabled = false
        heightCm = mBinding.height.text.toString()
        if (HeightValidator.isValid(heightCm, weight, systolic, diastolic, heartRate)) {
            submitForm()
        } else {
            mBinding.textInputHeight.error = (getString(R.string.height_range))
            buttonToSentVitals.isEnabled = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setDialogToConfirmVitalsRemoval()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setDialogToConfirmVitalsRemoval() {
        CustomDialogBundle().apply {
            titleViewMessage = getString(R.string.remove_vitals)
            textViewMessage = getString(R.string.cancel_vitals_dialog_message)
            rightButtonText = getString(R.string.end_vitals_dialog_message)
            leftButtonText = getString(R.string.keep_vitals_dialog_message)
            leftButtonAction = CustomFragmentDialog.OnClickAction.DISMISS
            rightButtonAction = CustomFragmentDialog.OnClickAction.END_VITALS
        }.let {
            createAndShowDialog(it, ApplicationConstants.DialogTAG.END_VITALS_TAG)
        }
    }

    private fun submitForm() {
        addVitalsToForm()
        viewModel.submitForm(vitals).observeOnce(this) { result ->
            when (result) {
                is Result.Success -> {
                    ToastUtil.success(getString(R.string.vitals_successfully))
                    setResult(RESULT_OK)
                    finish()
                }

                is Result.Error -> {
                    if (result.throwable is UnknownHostException) {
                        ToastUtil.error(getString(R.string.no_internet_connection))
                        mBinding.buttonToSentVitals.isEnabled = true
                    } else {
                        ToastUtil.error(getString(R.string.form_data_submit_error))
                    }
                }
                else -> ToastUtil.error(getString(R.string.form_data_submit_error))
            }
        }
    }

    private fun addVitalsToForm() {
        vitals.add(Vital(SYSTOLIC_FIELD_CONCEPT, systolic))
        vitals.add(Vital(DIASTOLIC_FIELD_CONCEPT, diastolic))
        vitals.add(Vital(HEART_RATE_FIELD_CONCEPT, heartRate))
        vitals.add(Vital(WEIGHT_FIELD_CONCEPT, weight))
        vitals.add(Vital(HEIGHT_FIELD_CONCEPT, heightCm))
    }

    private fun getHeightValueFromPreviousVisits() {
        viewModel.getLastHeightFromVisits().observe(this) { result ->
            when (result) {
                is Result.Success<String> -> {
                    val height = result.data
                    val heightField = mBinding.height
                    heightField.setText(height)
                }

                else -> throw IllegalStateException()
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

    private fun updateLanguageFields() {
        mBinding.textInputSystolic.hint = getString(R.string.systolic_label)
        mBinding.textInputDiastolic.hint = getString(R.string.diastolic_label)
        mBinding.textInputPulse.hint = getString(R.string.pulse_label)
        mBinding.textInputWeight.hint = getString(R.string.weight_value_label)
        mBinding.textInputHeight.hint = getString(R.string.height_value_label)
    }

    private fun askForPermissions() {
        if (ActivityCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS),
                2
            )
        }
    }
}
