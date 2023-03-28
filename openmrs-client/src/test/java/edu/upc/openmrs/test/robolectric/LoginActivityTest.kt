package edu.upc.openmrs.test.robolectric

import android.content.Intent
import android.os.Looper
import android.view.View
import android.view.autofill.AutofillValue
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import edu.upc.R
import edu.upc.openmrs.activities.community.contact.ContactUsActivity
import edu.upc.openmrs.activities.dashboard.DashboardActivity
import edu.upc.openmrs.activities.login.LoginActivity
import kotlinx.android.synthetic.main.fragment_login.*
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.jupiter.api.Disabled
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf


@RunWith(RobolectricTestRunner::class)
class LoginActivityTest {

    private lateinit var loginActivity: LoginActivity


    @Before
    fun setup() {
        WorkManagerTestInitHelper.initializeTestWorkManager(ApplicationProvider.getApplicationContext())
        loginActivity =
            Robolectric.buildActivity(LoginActivity::class.java).create().start().resume().get()

    }

    @Test
    fun clickingForgotPass_shouldStartContactUsActivity() {
        loginActivity.findViewById<View>(R.id.forgotPass).performClick()
        val expectedIntent = Intent(loginActivity, ContactUsActivity::class.java)
        val actual = shadowOf(RuntimeEnvironment.getApplication()).nextStartedActivity
        Assert.assertEquals(expectedIntent.component, actual.component)
    }

    @Test
    @Ignore
    fun clickingLoginButton_shouldStartDashboardActivity() {
        loginActivity.findViewById<View>(R.id.loginUsernameField)
            .autofill(AutofillValue.forText("admin"))
        loginActivity.findViewById<View>(R.id.loginPasswordField)
            .autofill(AutofillValue.forText("Admin123"))
        loginActivity.findViewById<View>(R.id.locationSpinner).autofill(AutofillValue.forList(1))

        loginActivity.findViewById<View>(R.id.loginButton).performClick()

        Thread.sleep(1_000)
        shadowOf(Looper.getMainLooper()).idle()

        val expectedIntent = Intent(loginActivity, DashboardActivity::class.java)
        val actual = shadowOf(RuntimeEnvironment.getApplication()).nextStartedActivity
        Assert.assertEquals(expectedIntent.component, actual.component)
    }
}
