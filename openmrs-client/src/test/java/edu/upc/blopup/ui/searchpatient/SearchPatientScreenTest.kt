package edu.upc.blopup.ui.searchpatient

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.models.Patient
import edu.upc.sdk.library.models.PatientIdentifier
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchPatientScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `when I go to Search Screen, my local saved patients have to be displayed`() {

        val patients = listOf(
            Patient().apply {
                identifiers = listOf(PatientIdentifier().apply {
                    id = 10001L
                    identifier = "10001"
                })
                display = "John Doe"
                birthdate = "1980-01-20T00:00:00.000+0000"
                birthdateEstimated = false
            },
            Patient().apply {
                identifiers = listOf(PatientIdentifier().apply {
                    id = 10002L
                    identifier = "10002"
                })
                display = "Jane Dune"
                birthdate = "1980-01-20T00:00:00.000+0000"
                birthdateEstimated = false
            }
        )

        composeTestRule.setContent {
            SyncedPatients(ResultUiState.Success(patients)) {}
        }

        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jane Dune").assertIsDisplayed()
    }

    @Test
    fun `when there is an error updating list of local patients, then an error should be display`() {
        composeTestRule.setContent {
            SyncedPatients(ResultUiState.Error) {}
        }

        composeTestRule.onNodeWithTag("error_message").assertIsDisplayed()
    }
}