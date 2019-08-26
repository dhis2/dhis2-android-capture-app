package org.dhis2.usescases.main;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Test;
import org.junit.runner.RunWith;

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
    }
}
