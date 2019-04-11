package org.dhis2.usescases.main;

import android.view.View;

import org.dhis2.R;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * QUADRAM. Created by ppajuelo on 10/04/2019.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Test
    public void errorLayoutDisplaysWhenErrorExist() {

        //GIVEN
        ActivityScenario scenario = ActivityScenario.launch(MainActivity.class);

        //WHEN
        onView(withId(R.id.errorLayout))
                .perform(setVisibility(true));

        //THEN;
        onView(withId(R.id.errorLayout))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    public static ViewAction setVisibility(final boolean value) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(View.class);
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.setVisibility(value ? View.VISIBLE : View.GONE);
            }
        };
    }
}
