package edu.upc.openmrs.test.integration

import android.content.Intent
import android.view.View
import android.view.autofill.AutofillValue
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.testing.WorkManagerTestInitHelper
import edu.upc.R
import edu.upc.openmrs.activities.login.LoginActivity
import edu.upc.openmrs.services.FormListService
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf


@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    private lateinit var loginActivity: LoginActivity


    @Before
    fun setup() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        loginActivity =
            Robolectric.buildActivity(LoginActivity::class.java).create().start().resume().get()

    }

    @Test
    fun clickingLoginButton_shouldStartFormListService() {
        loginActivity.findViewById<View>(R.id.loginUsernameField)
            .autofill(AutofillValue.forText("admin"))
        loginActivity.findViewById<View>(R.id.loginPasswordField)
            .autofill(AutofillValue.forText("Admin123"))
        loginActivity.findViewById<View>(R.id.locationSpinner).autofill(AutofillValue.forList(1))

        loginActivity.findViewById<View>(R.id.loginButton).performClick()

        val expectedIntent = Intent(loginActivity, FormListService::class.java)
        val actual = shadowOf(RuntimeEnvironment.getApplication()).nextStartedService
        Assert.assertEquals(expectedIntent.component, actual.component)
    }
}
