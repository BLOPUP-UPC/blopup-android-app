package edu.upc.blopup.ui.takingvitals

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.upc.blopup.ui.takingvitals.screens.TreatmentAdherence
import edu.upc.sdk.library.models.Treatment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VitalsActivityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `if fetch treatment fails, it should display the error`() {
        composeTestRule.setContent {
            TreatmentAdherence(treatments = ResultUiState.Error, createVisit = {}, treatmentAdherence = {}) {
            }
        }

        composeTestRule.onNodeWithText("Treatments could not be loaded.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try again").assertIsDisplayed()
        composeTestRule.onNodeWithText("Is the patient following these treatments?").assertIsNotDisplayed()

    }

    @Test
    fun `if fetch treatment is success, it should ask if patient is following those treatments`() {
        composeTestRule.setContent {
            TreatmentAdherence(treatments = ResultUiState.Success<List<Treatment>>(emptyList()), createVisit = {}, treatmentAdherence = {}) {
            }
        }

        composeTestRule.onNodeWithText("Is the patient following these treatments?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Treatments could not be loaded.").assertIsNotDisplayed()

    }
}