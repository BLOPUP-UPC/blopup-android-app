package edu.upc.blopup.tensiometer.readTensiometerMeasurement

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.observe
import edu.upc.blopup.changeLocale
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import edu.upc.blopup.exceptions.BluetoothConnectionException
import org.openmrs.mobile.R
import org.openmrs.mobile.databinding.ActivityReadTensiometerBinding

const val EXTRAS_MEASUREMENT = "measurement"
const val LOCATION_REQUEST = 1

@AndroidEntryPoint
class ReadTensiometerActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityReadTensiometerBinding
    private lateinit var mToolbar: Toolbar

    private val viewModel: ReadTensiometerViewModel by viewModels()
    private val showMeasurementsLauncher =
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
        val defaultLocale = resources.configuration.locales[0].language
        val language = intent?.extras?.getString("language") ?: defaultLocale
        changeLocale(baseContext, language)

        super.onCreate(savedInstanceState)

        mBinding = ActivityReadTensiometerBinding.inflate(layoutInflater)
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
            ActivityCompat.requestPermissions(this, locationPermission, LOCATION_REQUEST);
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
        viewModel.startListeningBluetoothConnection()
        viewModel.connectionViewState.observe(this) { state ->
            val icon = when (state) {
                ConnectionViewState.Disconnected -> R.drawable.ic_bluetooth_is_disconnected
                ConnectionViewState.Pairing -> R.drawable.ic_bluetooth_is_searching
            }
            mToolbar.findViewById<ImageView>(R.id.bluetooth_icon).setImageResource(icon)
        }
        viewModel.viewState.observe(this) { state ->
            when (state) {
                is TensiometerViewState.Error -> handleError(state.exception)
                is TensiometerViewState.Content -> {
                    val result = Intent().apply {
                        putExtra("systolic", state.measurement.systolic)
                        putExtra("diastolic", state.measurement.diastolic)
                        putExtra("heartRate", state.measurement.heartRate)
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
            getString(R.string.bt_exception_header).format(getString(exception.messageId)),
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction(getString(R.string.bt_exception_try_again)) { viewModel.startListeningBluetoothConnection() }
        }.show()
    }
}