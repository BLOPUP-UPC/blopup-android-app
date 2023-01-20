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
    private String firstName, familyName, estimatedYear, address1;

    @Rule
    public ActivityTestRule<AddEditPatientActivity> activityTestRule =
            new ActivityTestRule<>(AddEditPatientActivity.class);

    @Before
    public void initValidString () {
        this.firstName = "TestName";
        this.familyName = "TestSecondName";
        this.estimatedYear = "50";
        this.address1 = "BCN";
    }

    @Test
    public void registerPatientWithOnlyMandatoryFieldsSuccessfully () {
        onView(withId(R.id.firstName))
                .perform(typeText(this.firstName), closeSoftKeyboard())
                .check(matches(withText(this.firstName)));

        onView(withId(R.id.surname))
                .perform(typeText(this.familyName), closeSoftKeyboard())
                .check(matches(withText(this.familyName)));

        onView(withId(R.id.male)).perform(click()).check(matches(isChecked()));
        onView(withId(R.id.female)).check(matches(isNotChecked()));
        onView(withId(R.id.nonBinary)).check(matches(isNotChecked()));

        onView(withId(R.id.estimatedYear))
                .perform(scrollTo(), click())
                .perform(typeText(this.estimatedYear))
                .check(matches(withText(this.estimatedYear)));

        onView(withId(R.id.addressOne))
                .perform(scrollTo(), click())
                .perform(typeText(this.address1), closeSoftKeyboard())
                .check(matches(withText(this.address1)));

        onView(withId(R.id.actionSubmit)).perform(click());

        //Add check that you can see the patiend information after the registration
    }
}
