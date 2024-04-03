package edu.upc.openmrs.activities

import android.view.MenuItem
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.upc.blopup.ui.dashboard.DashboardActivity
import edu.upc.openmrs.activities.addeditpatient.AddEditPatientActivity
import edu.upc.openmrs.activities.syncedpatients.SyncedPatientsActivity
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class NavigationBarActivityTest {

    private val menuItem: MenuItem = mock(MenuItem::class.java)


    @Test
    fun `when I click in main menu icon, It should go to the dashboard`() {
        val navigationActivity = spy(NavigationBarActivity())

        `when`(menuItem.itemId).thenReturn(edu.upc.R.id.home_screen)

        doNothing().`when`(navigationActivity).openActivity(DashboardActivity::class.java)

        navigationActivity.launchSelectedActivity(menuItem)

        verify(navigationActivity).openActivity(DashboardActivity::class.java)
    }

    @Test
    fun `when I click in search a patient icon, It should go to the Search Patients Screen`() {
        val navigationActivity = spy(NavigationBarActivity())

        `when`(menuItem.itemId).thenReturn(edu.upc.R.id.search_patients)

        doNothing().`when`(navigationActivity).openActivity(SyncedPatientsActivity::class.java)

        navigationActivity.launchSelectedActivity(menuItem)

        verify(navigationActivity).openActivity(SyncedPatientsActivity::class.java)
    }

    @Test
    fun `when I click in register a patient icon, It should go to the Register a Patient Screen`() {
        val navigationActivity = spy(NavigationBarActivity())

        `when`(menuItem.itemId).thenReturn(edu.upc.R.id.register_patient)

        doNothing().`when`(navigationActivity).openActivity(AddEditPatientActivity::class.java)

        navigationActivity.launchSelectedActivity(menuItem)

        verify(navigationActivity).openActivity(AddEditPatientActivity::class.java)
    }
}