package org.dhis2.usescases.settings;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import org.dhis2.R;
import org.hamcrest.Matcher;

import static androidx.test.espresso.action.ViewActions.click;

public class ClickRefillButton implements ViewAction{
    ViewAction toClick = click();
    @Override
    public Matcher<View> getConstraints() {
        return toClick.getConstraints();
    }
    @Override
    public String getDescription() {
        return " click on custom image view";
    }
    @Override
    public void perform(UiController uiController, View view) {
        //toClick.perform(uiController, view.findViewById(R.id.refill));
        view.findViewById(R.id.refill).performClick();
    }
}
