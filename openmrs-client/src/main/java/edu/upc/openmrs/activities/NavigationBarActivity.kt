package edu.upc.openmrs.activities

import android.content.Intent
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import edu.upc.R
import edu.upc.blopup.ui.dashboard.DashboardActivity
import edu.upc.openmrs.activities.addeditpatient.AddEditPatientActivity
import edu.upc.openmrs.activities.syncedpatients.SyncedPatientsActivity

open class NavigationBarActivity : ACBaseActivity() {
    open fun setBottomNavigationBar(bottomNavigationView: BottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener { item ->
            launchSelectedActivity(item)
        }
    }

    fun launchSelectedActivity(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home_screen -> { openActivity(DashboardActivity::class.java) }
            R.id.search_patients -> { openActivity(SyncedPatientsActivity::class.java) }
            R.id.register_patient -> { openActivity(AddEditPatientActivity::class.java) }
        }
        return true
    }

    fun openActivity(activity: Class<*>) {
        val intent = Intent(this, activity)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}
