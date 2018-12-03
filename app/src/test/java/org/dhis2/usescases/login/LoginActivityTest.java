package org.dhis2.usescases.login;

import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;

import org.dhis2.App;
import org.dhis2.AppComponent;
import org.dhis2.AppModule;
import org.dhis2.DaggerAppComponent;
import org.dhis2.R;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import javax.inject.Singleton;

import dagger.Component;
import testUtils.DaggerActivityTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(RobolectricTestRunner.class)
public class LoginActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule =
            new DaggerActivityTestRule<>(LoginActivity.class, (application, activity) -> {
                App app = (App) application;
                AppComponent mTestAppComponent = DaggerAppComponent.builder()
                        .appModule(new AppModule(app))
                        .build();

                app.setTestComponent(mTestAppComponent);
            });

    @Singleton
    @Component(modules = {AppModule.class})
    interface TestAppComponent extends AppComponent {
    }


    @Test
    public void showWarningRuleActionTest() {
        onView(withId(R.id.server_url)).perform(clearText(), typeText("https://play.dhis2.org/android-current"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.user_name)).perform(clearText(), typeText("android"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.user_pass)).perform(clearText(), typeText("Android123"), ViewActions.closeSoftKeyboard());

        onView(withId(R.id.login)).check(matches(isDisplayed()));
    }
}
