package edu.upc;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.upc.openmrs.activities.addeditpatient.AddEditPatientActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreatePatientTest {

    @Rule
    public ActivityScenarioRule<AddEditPatientActivity> activityRule =
            new ActivityScenarioRule<>(AddEditPatientActivity.class);

    @Test
    @Ignore
    public void registerPatientWithOnlyMandatoryFieldsSuccessfully() {
        String firstName = "Queens", familyName = "Scarlet", estimatedYear = "50";

        onView(withId(R.id.firstName))
                .perform(typeText(firstName), closeSoftKeyboard())
                .check(matches(withText(firstName)));

        onView(withId(R.id.surname))
                .perform(typeText(familyName), closeSoftKeyboard())
                .check(matches(withText(familyName)));

        onView(withId(R.id.male)).perform(click()).check(matches(isChecked()));
        onView(withId(R.id.female)).check(matches(isNotChecked()));
        onView(withId(R.id.nonBinary)).check(matches(isNotChecked()));

        onView(withId(R.id.addressOne)).perform(scrollTo());
        onView(withId(R.id.estimatedYear))
                .perform(click(), typeText(estimatedYear));

        onView(withId(R.id.submitButton)).perform(click());
    }

    @Test
    @Ignore
    public void registerPatientFailsWhenCreateItWithoutGender() {
        String firstName = "Juan", familyName = "Macson", estimatedYear = "99";

        onView(withId(R.id.firstName))
                .perform(typeText(firstName), closeSoftKeyboard())
                .check(matches(withText(firstName)));

        onView(withId(R.id.surname))
                .perform(typeText(familyName), closeSoftKeyboard())
                .check(matches(withText(familyName)));

        onView(withId(R.id.addressOne)).perform(scrollTo());
        onView(withId(R.id.estimatedYear))
                .perform(click(), typeText(estimatedYear));


        onView(withId(R.id.gendererror))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.submitButton)).perform(click());
        onView(withId(R.id.gendererror))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
}
