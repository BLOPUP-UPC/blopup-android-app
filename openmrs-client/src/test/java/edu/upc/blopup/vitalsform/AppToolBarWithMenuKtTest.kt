package edu.upc.blopup.vitalsform


import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.upc.blopup.ui.takingvitals.VitalsActivity
import edu.upc.openmrs.activities.settings.SettingsActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppToolBarWithMenuKtTest{

    //check if we can generify this as the AppToolBarWithMenu should be able to be used in multiple places
    @get:Rule
    val composeRuleWithActivity = createAndroidComposeRule<VitalsActivity>()


    @Test
    fun myTest() {
        composeRuleWithActivity.onNodeWithTag("back_button").performClick()

        assert(composeRuleWithActivity.activity.isFinishing)
    }


    @Test
    fun `should go to Settings Activity  when clicking this option in the menu dropdown`() {

        Intents.init()

        composeRuleWithActivity.onNodeWithContentDescription("Options").performClick()
        composeRuleWithActivity.onNodeWithText("Settings").performClick()

        Intents.intended(IntentMatchers.hasComponent(SettingsActivity::class.java.name))
        Intents.release()
    }
}
