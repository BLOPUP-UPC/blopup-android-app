package edu.upc;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.not;

import android.content.Context;
import android.view.View;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.material.textfield.TextInputLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.upc.openmrs.activities.addeditpatient.AddEditPatientActivity;
import edu.upc.openmrs.activities.addeditpatient.countryofbirth.Country;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreatePatientTest {

    @Rule
    public ActivityScenarioRule<AddEditPatientActivity> activityRule =
            new ActivityScenarioRule<>(AddEditPatientActivity.class);

    @Test
    public void registerPatientWithOnlyMandatoryFieldsSuccessfully() {
        String name = "Roger", familyName = "Federer", estimatedYear = "38";
        Country countrySelected = Country.AFGHANISTAN;

        onView(withId(R.id.firstName))
                .perform(typeText(name), closeSoftKeyboard())
                .check(matches(withText(name)));

        onView(withId(R.id.surname))
                .perform(typeText(familyName), closeSoftKeyboard())
                .check(matches(withText(familyName)));

        onView(withId(R.id.male)).perform(click()).check(matches(isChecked()));
        onView(withId(R.id.female)).check(matches(isNotChecked()));
        onView(withId(R.id.nonBinary)).check(matches(isNotChecked()));

        onView(withId(R.id.estimatedYear))
                .perform(click(), typeText(estimatedYear));

        onView(withId(R.id.country_of_birth))
                .perform(closeSoftKeyboard())
                .perform(scrollTo())
                .perform(click());
        onView(withText(countrySelected.getLabel()))
                .perform(click());
        onView(withId(R.id.country_of_birth))
                .check(matches(withText(countrySelected.getLabel())));

        onView(withId(R.id.language_spinner))
                .perform(closeSoftKeyboard())
                .perform(scrollTo())
                .perform(click());

        onData(anything())
                .atPosition(3)
                .perform(click());

        onView(withId(R.id.record_legal_consent))
                .perform(scrollTo())
                .check(matches(isEnabled()))
                .perform(click());

        onView(withId(R.id.record))
                .perform(click());

        onView(withId(R.id.recordingInProgress))
                .check(matches(isDisplayed()));

        onView(withId(R.id.play_pause))
                .check(matches(isDisplayed()))
                .perform(click())
                .perform(click());

        onView(withId(R.id.stop))
                .check(matches(not(isClickable())));

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.stop))
                .perform(click());

        onView(withId(R.id.record_consent_saved))
                .check(matches(isDisplayed()));

        onView(withId(R.id.record_legal_consent))
            .check(matches(withText(R.string.record_again_legal_consent)));

        onView(withId(R.id.submitButton))
                .perform(scrollTo())
                .perform(click());
    }

    @Test
    public void registerPatientFailsWhenCreateItWithoutGender() {
        onView(withId(R.id.submitButton))
                .perform(scrollTo())
                .perform(click());

        onView(withId(R.id.gendererror))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Test
    public void registerPatientFailsWhenCreateItWithoutFullName() {
        onView(withId(R.id.firstName))
                .perform(clearText());

        onView(withId(R.id.surname))
                .perform(clearText());

        onView(withId(R.id.submitButton))
                .perform(scrollTo())
                .perform(click());

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        String errorMessage = context.getString(R.string.emptyerror);

        onView(withId(R.id.textInputLayoutFirstName))
                .check(matches(isDisplayed()))
                .check(matches(hasTextInputLayoutHintText(errorMessage)));

        onView(withId(R.id.textInputLayoutSurname))
                .check(matches(isDisplayed()))
                .check(matches(hasTextInputLayoutHintText(errorMessage)));
    }

    @Test
    public void registerPatientFailsWhenCreateItWithoutACountryOfBirth() {

        onView(withId(R.id.submitButton))
                .perform(scrollTo())
                .perform(click());

        onView(withId(R.id.country_of_birth_error))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Test
    public void registerPatientFailsWhenCreateItWithoutLegalConsent() {
        onView(withId(R.id.submitButton))
                .perform(scrollTo())
                .perform(click());

        onView(withId(R.id.record_consent_error))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    public static TypeSafeMatcher<View> hasTextInputLayoutHintText(final String expectedErrorText) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextInputLayout)) {
                    return false;
                }

                CharSequence error = ((TextInputLayout) view).getError();

                if (error == null) {
                    return false;
                }

                String errorString = error.toString();

                return expectedErrorText.equals(errorString);
            }

            @Override
            public void describeTo(Description description) {
            }
        };
    }

    public static Matcher<Object> withSpinnerSelectedItem(Matcher<Object> itemMatcher) {
        return new TypeSafeMatcher<Object>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("is a spinner with selected item: ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(Object item) {
                return itemMatcher.matches(item);
            }
        };
    }
}
