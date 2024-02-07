package edu.upc.blopup.vitalsform


import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BloodPressureScreenKtTest {

    @get:Rule
    val composeRuleWithActivity = createAndroidComposeRule<VitalsActivity>()


    @Test
    fun myTest() {
        composeRuleWithActivity.onNodeWithTag("back_button").performClick()

        assert(composeRuleWithActivity.activity.isFinishing)
    }
}
