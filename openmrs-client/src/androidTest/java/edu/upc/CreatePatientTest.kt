package edu.upc

import android.view.View
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.material.textfield.TextInputLayout
import edu.upc.openmrs.activities.addeditpatient.AddEditPatientActivity
import edu.upc.openmrs.activities.addeditpatient.countryofbirth.Country
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
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
            .atPosition(3)
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
    fun registerPatientFailsWhenCreateItWithoutGender() {
        onView(withId(R.id.submitButton))
            .perform(scrollTo())
            .perform(click())
        onView(withId(R.id.gendererror))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun registerPatientFailsWhenCreateItWithoutFullName() {
        onView(withId(R.id.firstName))
            .perform(ViewActions.clearText())
        onView(withId(R.id.surname))
            .perform(ViewActions.clearText())
        onView(withId(R.id.submitButton))
            .perform(scrollTo())
            .perform(click())
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val errorMessage = context.getString(R.string.emptyerror)
        onView(withId(R.id.textInputLayoutFirstName))
            .check(matches(isDisplayed()))
            .check(matches(hasTextInputLayoutHintText(errorMessage)))
        onView(withId(R.id.textInputLayoutSurname))
            .check(matches(isDisplayed()))
            .check(matches(hasTextInputLayoutHintText(errorMessage)))
    }

    @Test
    fun registerPatientFailsWhenCreateItWithoutACountryOfBirth() {
        onView(withId(R.id.submitButton))
            .perform(scrollTo())
            .perform(click())
        onView(withId(R.id.country_of_birth_error))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    @Test
    fun registerPatientFailsWhenCreateItWithoutLegalConsent() {
        onView(withId(R.id.submitButton))
            .perform(scrollTo())
            .perform(click())
        onView(withId(R.id.record_consent_error))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }

    fun hasTextInputLayoutHintText(expectedErrorText: String): TypeSafeMatcher<View?>? {
        return object : TypeSafeMatcher<View?>() {
            override fun matchesSafely(view: View?): Boolean {
                if (view !is TextInputLayout) {
                    return false
                }
                val error = view.error ?: return false
                val errorString = error.toString()
                return expectedErrorText == errorString
            }

            override fun describeTo(description: Description) {}
        }
    }

    fun withSpinnerSelectedItem(itemMatcher: Matcher<Any?>): Matcher<Any?>? {
        return object : TypeSafeMatcher<Any?>() {
            override fun describeTo(description: Description) {
                description.appendText("is a spinner with selected item: ")
                itemMatcher.describeTo(description)
            }

            override fun matchesSafely(item: Any?): Boolean {
                return itemMatcher.matches(item)
            }
        }
    }
}