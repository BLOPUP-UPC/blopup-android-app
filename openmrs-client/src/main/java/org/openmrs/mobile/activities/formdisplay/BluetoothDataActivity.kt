package org.openmrs.mobile.activities.formdisplay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class BluetoothDataActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val result = Intent().apply {
            putExtra("systolic", 112)
            putExtra("diastolic", 72)
            putExtra("heartRate", 68)
        }
        setResult(RESULT_OK, result)
        finish()
    }
}