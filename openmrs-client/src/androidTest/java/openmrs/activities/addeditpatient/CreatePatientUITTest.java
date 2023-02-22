package openmrs.activities.addeditpatient;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.upc.R;
import edu.upc.openmrs.activities.addeditpatient.AddEditPatientActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreatePatientUITTest {
    private String firstName, familyName, estimatedYear;

    @Rule
    public ActivityTestRule<AddEditPatientActivity> AddEditPatientActivityTestRule =
            new ActivityTestRule<>(AddEditPatientActivity.class);

    @Before
    public void initValidString() {
        this.firstName = "Queens";
        this.familyName = "Scarlet";
        this.estimatedYear = "50";
    }

    @Test
    public void registerPatientWithOnlyMandatoryFieldsSuccessfully() {
        onView(withId(R.id.firstName))
                .perform(typeText(this.firstName), closeSoftKeyboard())
                .check(matches(withText(this.firstName)));

        onView(withId(R.id.surname))
                .perform(typeText(this.familyName), closeSoftKeyboard())
                .check(matches(withText(this.familyName)));

        onView(withId(R.id.male)).perform(click()).check(matches(isChecked()));
        onView(withId(R.id.female)).check(matches(isNotChecked()));
        onView(withId(R.id.nonBinary)).check(matches(isNotChecked()));

        onView(withId(R.id.addressOne)).perform(scrollTo());
        onView(withId(R.id.estimatedYear))
                .perform(click(), typeText(this.estimatedYear));

        onView(withId(R.id.submitButton)).perform(click());
    }
}
