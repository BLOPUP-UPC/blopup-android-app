package edu.upc.blopup.scale.readScaleMeasurement

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
import edu.upc.databinding.ActivityReadScaleBinding

const val EXTRAS_WEIGHT = "weight"
const val LOCATION_REQUEST = 1

@AndroidEntryPoint
class ReadWeightActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityReadScaleBinding
    private lateinit var mToolbar: Toolbar

    private val viewModel: ReadScaleViewModel by viewModels()

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
        viewModel.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.disconnect()
    }

    private fun setUpToolbar() {
        mToolbar = mBinding.toolbarInclude.toolbar
        mToolbar.title = getString(R.string.toolbar_read_measurement_title)
        mToolbar.findViewById<ImageView>(R.id.bluetooth_icon)
            .setImageResource(R.drawable.ic_bluetooth_is_searching)
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
        hardcodeBluetoothDataToggle.check({
            hardcodeBluetoothData()
        })

        viewModel.startListeningBluetoothConnection()
        observeWeightData()
    }

    private fun observeWeightData() {
        viewModel.viewState.observe(this) { state ->
            when (state) {
                is ScaleViewState.Error -> handleError(state.exception)
                is ScaleViewState.Content -> {
                    val result = Intent().apply {
                        putExtra(EXTRAS_WEIGHT, state.weightMeasurement.weight)
                    }
                    setResult(RESULT_OK, result)
                    finish()
                }
            }
        }
    }

    private fun hardcodeBluetoothData() {
        val result = Intent().apply {
            putExtra(EXTRAS_WEIGHT, 56.6f)
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
