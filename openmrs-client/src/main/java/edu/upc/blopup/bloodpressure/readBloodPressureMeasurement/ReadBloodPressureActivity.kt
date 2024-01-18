package edu.upc.blopup.bloodpressure.readBloodPressureMeasurement

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.R
import edu.upc.blopup.exceptions.BluetoothConnectionException
import edu.upc.blopup.toggles.check
import edu.upc.blopup.toggles.hardcodeBluetoothDataToggle
import edu.upc.databinding.ActivityReadBloodPressureBinding
import edu.upc.openmrs.utilities.observeOnce

const val EXTRAS_SYSTOLIC = "systolic"
const val EXTRAS_DIASTOLIC = "diastolic"
const val EXTRAS_HEART_RATE = "heartRate"
const val LOCATION_REQUEST = 1

@AndroidEntryPoint
class ReadBloodPressureActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityReadBloodPressureBinding
    private lateinit var mToolbar: Toolbar

    private val viewModel: ReadBloodPressureViewModel by viewModels()
    private val locationPermission = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    private val bluetoothPermission = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityReadBloodPressureBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setUpToolbar()
        askPermissions()
        startReading()
    }

    override fun onStop() {
        super.onStop()
        viewModel.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disconnect()
    }

    private fun setUpToolbar() {
        mToolbar = mBinding.toolbarInclude.toolbar
        mToolbar.title = getString(R.string.toolbar_read_measurement_title)
        setSupportActionBar(mToolbar)
    }

    private fun askPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, locationPermission, LOCATION_REQUEST)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && (
                    ActivityCompat.checkSelfPermission(
                        this,
                        bluetoothPermission[0]
                    ) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(
                                this,
                                bluetoothPermission[1]
                            ) != PackageManager.PERMISSION_GRANTED
                    )
        ) {
            ActivityCompat.requestPermissions(this, bluetoothPermission, LOCATION_REQUEST)
        }
    }

    private fun startReading() {
        hardcodeBluetoothDataToggle.check(onToggleEnabled = {
            hardcodeBluetoothData()
        })

        setUpSwipeAdapter()

        viewModel.startListeningBluetoothConnection()
        observeBluetoothConnectionState()
        observeBloodPressureData()
    }

    private fun observeBloodPressureData() {
        viewModel.viewState.observeOnce(this) { state ->
            when (state) {
                is BloodPressureViewState.Error -> handleError(state.exception)
                is BloodPressureViewState.Content -> {
                    val result = Intent().apply {
                        putExtra(EXTRAS_SYSTOLIC, state.measurement.systolic)
                        putExtra(EXTRAS_DIASTOLIC, state.measurement.diastolic)
                        putExtra(EXTRAS_HEART_RATE, state.measurement.heartRate)
                    }
                    setResult(RESULT_OK, result)
                    finish()
                }
            }
        }
    }

    private fun observeBluetoothConnectionState() {
        viewModel.connectionViewState.observe(this) { state ->
            val icon = when (state) {
                ConnectionViewState.Disconnected -> R.drawable.ic_bluetooth_is_disconnected
                ConnectionViewState.Pairing -> R.drawable.ic_bluetooth_is_searching
            }
            mToolbar.findViewById<ImageView>(R.id.bluetooth_icon).setImageResource(icon)
        }
    }

    private fun setUpSwipeAdapter() {
        val adapter = BloodPressureInstructionsAdapter()
        val myPager = mBinding.viewPagerBloodPressure
        myPager.adapter = adapter
        myPager.currentItem = 0
    }

    private fun hardcodeBluetoothData() {
        val result = Intent().apply {
            putExtra(EXTRAS_SYSTOLIC, (80..250).random())
            putExtra(EXTRAS_DIASTOLIC, (50..99).random())
            putExtra(EXTRAS_HEART_RATE, (55..120).random())
        }
        setResult(RESULT_OK, result)
    }

    private fun handleError(exception: BluetoothConnectionException) {
        Snackbar.make(
            mBinding.coordinatorLayout,
            getString(R.string.bt_exception_header).format(getString(exception.messageId)),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(getString(R.string.bt_exception_try_again)) { viewModel.startListeningBluetoothConnection() }
        }.show()
    }
}