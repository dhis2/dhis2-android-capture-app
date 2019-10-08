package org.dhis2.usescases.main;

import android.view.Gravity;
import android.view.View;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import org.dhis2.R;
import org.dhis2.usescases.main.program.ProgramFragment;
import org.dhis2.usescases.main.program.ProgramViewModel;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hisp.dhis.android.core.program.ProgramType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA);

    @Test
    public void checkMainActivityIsVisible() {
        ActivityScenario<MainActivity> scenario = rule.getScenario();
        scenario.moveToState(Lifecycle.State.RESUMED);

        onView(withId(R.id.menu)).perform(ViewActions.click());
        onView(withId(R.id.main_drawer_layout)).check(((view, noViewFoundException) ->
                Assert.assertTrue(((DrawerLayout) view).isDrawerOpen(Gravity.START))
        ));
    }

    @Test
    public void checkProgramIsLoaded() {
        ActivityScenario<MainActivity> scenario = rule.getScenario();
        scenario.moveToState(Lifecycle.State.RESUMED);

        List<ProgramViewModel> testProgramList = new ArrayList<>();
        testProgramList.add(
                ProgramViewModel.create("programA", "TestingProgram1", "#f50057", "ic_agriculture_negative", 1987, "typeUid",
                        "TypeTest", ProgramType.WITH_REGISTRATION.name(), "Test description", true, true)
        );

        scenario.onActivity(activity -> {
            try {
                ((ProgramFragment) activity.activeFragment).swapProgramModelData().accept(testProgramList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        /*onView(withId(R.id.program_recycler))
                .check(matches(withViewAtPosition(1,hasDescendant(allOf(withId(R.id.program_image),isDisplayed())))));*/


    }

    public static Matcher<View> withViewAtPosition(final int position, final Matcher<View> itemMatcher) {
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                itemMatcher.describeTo(description);

            }

            @Override
            protected boolean matchesSafely(RecyclerView recyclerView) {
                final RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                return viewHolder != null && itemMatcher.matches(viewHolder.itemView);
            }
        };
    }

}
