package edu.upc;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.upc.openmrs.activities.login.LoginActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginTest {
    private String userName, password;

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void initValidString() {
        this.userName = "admin";
        this.password = "Admin123";
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
}
