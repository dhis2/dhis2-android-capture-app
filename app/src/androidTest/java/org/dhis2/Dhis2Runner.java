package org.dhis2;

import android.os.Bundle;
import android.os.StrictMode;
import androidx.test.runner.AndroidJUnitRunner;

public class Dhis2Runner extends AndroidJUnitRunner {

    @Override
    public void onCreate(Bundle arguments) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        super.onCreate(arguments);
    }

    @Override public void onStart() {
    //    RxJavaPlugins.setInitComputationSchedulerHandler(
    //            Rx2Idler.create("RxJava 2.x Computation Scheduler"));
    //    RxJavaPlugins.setInitIoSchedulerHandler(
    //            Rx2Idler.create("RxJava 2.x IO Scheduler"));
        super.onStart();
    }
}