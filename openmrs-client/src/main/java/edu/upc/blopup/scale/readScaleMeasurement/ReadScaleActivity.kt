package edu.upc.blopup.scale.readScaleMeasurement

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.observe
import edu.upc.blopup.exceptions.BluetoothConnectionException
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.openmrs.mobile.R
import org.openmrs.mobile.activities.ACBaseActivity
import org.openmrs.mobile.databinding.ActivityReadScaleBinding

const val EXTRAS_MEASUREMENT = "weightMeasurement"
const val LOCATION_REQUEST = 1

@AndroidEntryPoint
class ReadWeightActivity : ACBaseActivity() {

    private lateinit var mBinding: ActivityReadScaleBinding
    private lateinit var mToolbar: Toolbar

    private val readScaleViewModel: ReadScaleViewModel by viewModels()

    private val showWeightLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            setResult(RESULT_OK, result.data)
            finish()
        }
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

        mBinding = ActivityReadScaleBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setUpToolbar()
        askPermissions()
        startReading()
    }

    override fun onStop() {
        super.onStop()
        readScaleViewModel.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        readScaleViewModel.disconnect()
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
//        showProgressDialog(R.string.action_connecting_to_the_hardware)
        readScaleViewModel.startListeningBluetoothConnection()
        readScaleViewModel.connectionViewState.observe(this) { state ->
            val icon = when (state) {
                ConnectionViewState.Disconnected -> R.drawable.ic_bluetooth_is_disconnected
                ConnectionViewState.Pairing -> R.drawable.ic_bluetooth_is_searching
            }
            mToolbar.findViewById<ImageView>(R.id.bluetooth_icon).setImageResource(icon)
//            dismissCustomFragmentDialog()
        }
        readScaleViewModel.viewState.observe(this) { state ->
            when (state) {
                is ScaleViewState.Error -> handleError(state.exception)
                is ScaleViewState.Content -> {
                    val result = Intent().apply {
                        putExtra("weight", state.weightMeasurement.weight)
                    }
                    setResult(RESULT_OK, result)
                    finish()
                }
            }
        }
    }

    private fun handleError(exception: BluetoothConnectionException) {
        Snackbar.make(
            mBinding.coordinatorLayout,
            "Error".format(getString(exception.messageId)),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(getString(R.string.bt_exception_try_again)) { readScaleViewModel.startListeningBluetoothConnection() }
        }.show()
    }
}
