package edu.upc.openmrs.utilities

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.upc.R
import edu.upc.openmrs.activities.addeditpatient.AddEditPatientActivity
import edu.upc.openmrs.activities.dashboard.DashboardActivity
import edu.upc.openmrs.activities.syncedpatients.SyncedPatientsActivity

object NavigationBarUtils {
    fun setBottomNavigationBar(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_screen -> startNewActivity(bottomNavigationView, DashboardActivity::class.java)
                R.id.search_patients -> startNewActivity(bottomNavigationView, SyncedPatientsActivity::class.java)
                R.id.register_patient -> startNewActivity(bottomNavigationView, AddEditPatientActivity::class.java)
            }
            true
        }
    }

    private fun startNewActivity(bottomNavigationView: BottomNavigationView, activityClass: Class<*>) {
        val intent = Intent(bottomNavigationView.context, activityClass)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        bottomNavigationView.context.startActivity(intent)
        (bottomNavigationView.context as Activity).overridePendingTransition(0, 0)
    }
}