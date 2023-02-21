import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;

import android.widget.EditText;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.upc.R;
import edu.upc.openmrs.activities.addeditpatient.AddEditPatientActivity;
import edu.upc.openmrs.activities.login.LoginActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreatePatientUITTest {
    private String firstName, familyName, estimatedYear, address1, userName, password, middleName, dob, phoneNumber;

    @Rule
    public ActivityTestRule<LoginActivity> activityTestRule =
            new ActivityTestRule<>(LoginActivity.class);

    @Rule
    public ActivityTestRule<AddEditPatientActivity> AddEditPatientActivityTestRule =
            new ActivityTestRule<>(AddEditPatientActivity.class);

    @Before
    public void initValidString() {
        this.firstName = "Queens";
        this.familyName = "Scarlet";
        this.middleName = "Bishop";
        this.estimatedYear = "50";
        this.address1 = "BCN";
        userName = "admin";
        password = "Admin123";
        dob = "140520000";
        phoneNumber = "+3460234343953";
    }

    @Test
    public void loginUserSuccessfully() {
        onView(withId(R.id.loginUsernameField))
                .perform(typeText(this.userName), closeSoftKeyboard())
                .check(matches(withText(this.userName)));

        onView(withId(R.id.loginPasswordField))
                .perform(typeText(this.password), closeSoftKeyboard())
                .check(matches(withText(this.password)));

        onView(withId(R.id.locationSpinner)).perform(click());
        onData(anything()).atPosition(1).perform(click());
        onView(withId(R.id.locationSpinner)).check(matches(withSpinnerText(containsString("Inpatient"))));

        onView(withId(R.id.loginButton)).perform(click());

    }

    @Test
    public void registerPatientWithOnlyMandatoryFieldsSuccessfully() {

        onView(withId(R.id.firstName))
                .perform(typeText(this.firstName), closeSoftKeyboard())
                .check(matches(withText(this.firstName)));


        onView(withId(R.id.surname))
                .perform(typeText(this.familyName), closeSoftKeyboard())
                .check(matches(withText(this.familyName)));

        onView(withId(R.id.middlename))
                .perform(typeText(middleName), closeSoftKeyboard())
                .check(matches(withText(middleName)));

        onView(withId(R.id.male)).perform(click()).check(matches(isChecked()));
        onView(withId(R.id.female)).check(matches(isNotChecked()));
        onView(withId(R.id.nonBinary)).check(matches(isNotChecked()));


        onView(withId(R.id.phoneNumber))
                .perform(typeText(phoneNumber), closeSoftKeyboard())
                .check(matches(withText(phoneNumber)));

        onView(withId(R.id.estimatedYear))
                .perform(scrollTo(), click())
                .perform(typeText(this.estimatedYear))
                .check(matches(withText(this.estimatedYear)));

        onView(withId(R.id.addressOne))
                .perform(scrollTo(), click())
                .perform(typeText(this.address1), closeSoftKeyboard())
                .check(matches(withText(this.address1)));

        onView(withId(R.id.submitButton)).perform(click());

        //Add check that you can see the patiend information after the registration
    }
}
