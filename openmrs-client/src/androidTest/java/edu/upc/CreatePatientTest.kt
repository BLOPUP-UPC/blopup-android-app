package edu.upc

import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import edu.upc.openmrs.activities.addeditpatient.AddEditPatientActivity
import edu.upc.openmrs.activities.addeditpatient.countryofbirth.Country
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test


class CreatePatientTest {

    @get:Rule
    var activityRule = ActivityScenarioRule(
        AddEditPatientActivity::class.java
    )

    @Test
    fun registerPatientWithOnlyMandatoryFieldsSuccessfully() {
        val name = "Roger"
        val familyName = "Federer"
        val estimatedYear = "38"
        val countrySelected = Country.AFGHANISTAN

        onView(withId(R.id.firstName))
            .perform(ViewActions.typeText(name), ViewActions.closeSoftKeyboard())
            .check(matches(withText(name)))

        onView(withId(R.id.surname))
            .perform(ViewActions.typeText(familyName), ViewActions.closeSoftKeyboard())
            .check(matches(withText(familyName)))

        onView(withId(R.id.male)).perform(click())
            .check(matches(isChecked()))

        onView(withId(R.id.female))
            .check(matches(isNotChecked()))
        onView(withId(R.id.nonBinary))
            .check(matches(isNotChecked()))

        onView(withId(R.id.estimatedYear))
            .perform(click(), ViewActions.typeText(estimatedYear))

        onView(withId(R.id.country_of_birth))
            .perform(ViewActions.closeSoftKeyboard())
            .perform(scrollTo())
            .perform(click())
        onView(withText(countrySelected.label))
            .perform(click())
        onView(withId(R.id.country_of_birth))
            .check(matches(withText(countrySelected.label)))

        onView(withId(R.id.language_spinner))
            .perform(ViewActions.closeSoftKeyboard())
            .perform(scrollTo())
            .perform(click())

        onData(CoreMatchers.anything())
            .atPosition(2)
            .perform(click())

        onView(withId(R.id.record_legal_consent))
            .perform(scrollTo())
            .perform(click())
        onView(withId(R.id.record))
            .perform(click())
        onView(withId(R.id.recordingInProgress))
            .check(matches(isDisplayed()))
        onView(withId(R.id.play_pause))
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(click())
        onView(withId(R.id.stop))
            .check(matches(not(isClickable())))

        try {
            Thread.sleep(60000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        onView(withId(R.id.stop))
            .perform(click())
        onView(withId(R.id.record_consent_saved))
            .check(matches(isDisplayed()))
        onView(withId(R.id.record_legal_consent))
            .check(matches(withText(R.string.record_again_legal_consent)))

        onView(withId(R.id.submitButton))
            .perform(scrollTo())
            .perform(click())
    }

    @Test
    fun registerPatientFailsWhenCreateItWithoutGenderCountryOfBirthAndLegalConsent() {
        onView(withId(R.id.submitButton))
            .perform(scrollTo())
            .perform(click())
        onView(withId(R.id.gendererror))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.country_of_birth_error))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.record_consent_error))
            .perform(scrollTo())
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }
}