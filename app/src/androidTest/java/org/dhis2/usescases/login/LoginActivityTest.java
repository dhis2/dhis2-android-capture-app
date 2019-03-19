package org.dhis2.usescases.login;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.dhis2.R;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    private static final String TEST_URL = "https://play.dhis2.org/android-current";
    private static final String TEST_USERNAME = "android";
    private static final String TEST_PASS = "Android123";


    @Test
    public void loginButtonAppearsOnlyWhenUrlUsernamePassAreProvided() {

        ViewInteraction serverUrlEditText = onView(
                allOf(withId(R.id.server_url_edit),
                        childAtPosition(
                                childAtPosition(withId(R.id.server_url), 0), 0),
                        isDisplayed()));
        serverUrlEditText.perform(replaceText(TEST_URL), pressImeActionButton());

        ViewInteraction userNameEditText = onView(
                allOf(withId(R.id.user_name_edit),
                        childAtPosition(
                                childAtPosition(withId(R.id.user_name), 0), 0),
                        isDisplayed()));
        userNameEditText.perform(replaceText(TEST_USERNAME), pressImeActionButton());

        ViewInteraction passEditText = onView(
                allOf(childAtPosition(
                        childAtPosition(withId(R.id.user_pass), 0), 0),
                        isDisplayed()));
        passEditText.perform(replaceText(TEST_PASS), pressImeActionButton());

        ViewInteraction button = onView(
                allOf(withId(R.id.login),
                        isDisplayed()));

        button.check(matches(isDisplayed()));
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
