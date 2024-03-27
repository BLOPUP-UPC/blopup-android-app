package edu.upc.blopup.ui.location

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.upc.blopup.ui.ResultUiState
import edu.upc.sdk.library.databases.entities.LocationEntity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `if fetch current location fails, it should display the error`() {
        composeTestRule.setContent {
            LocationDialog(show = true, onDialogClose = {}, currentLocation = ResultUiState.Error, locationsList = ResultUiState.Error, onSetLocation = {})
        }

        composeTestRule.onNodeWithText("Error updating current location. Please, try again later.").assertIsDisplayed()
        composeTestRule.onNodeWithText("SELECT LOCATION").assertIsNotDisplayed()
    }

    @Test
    fun `if fetch current location is success, it should display current location and location list`() {

        val locationList = listOf(LocationEntity(display = "Hospital One"), LocationEntity(display = "Hospital Two"), LocationEntity(display = "Hospital Three"))

        composeTestRule.setContent {
            LocationDialog(show = true, onDialogClose = {}, currentLocation = ResultUiState.Success("Hospital Two"), locationsList = ResultUiState.Success(locationList), onSetLocation = {})
        }

        composeTestRule.onNodeWithText("Error updating current location. Please, try again.").assertIsNotDisplayed()
        composeTestRule.onAllNodesWithText("Hospital Two").assertCountEquals(2)
        composeTestRule.onNodeWithText("Hospital One").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hospital Three").assertIsDisplayed()
        composeTestRule.onNodeWithText("CANCEL").assertIsDisplayed()
        composeTestRule.onNodeWithText("SELECT LOCATION").assertIsDisplayed()

    }
}