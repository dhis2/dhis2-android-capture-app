package org.dhis2;

import android.util.Log;
import androidx.test.runner.AndroidJUnitRunner;
import com.squareup.rx2.idler.Rx2Idler;
import io.reactivex.plugins.RxJavaPlugins;

public class Dhis2Runner extends AndroidJUnitRunner {

    @Override public void onStart() {
        super.onStart();
        Log.i("DhisRunner", "onStart");
        RxJavaPlugins.setInitComputationSchedulerHandler(Rx2Idler.create("RxJava 2.x Computation Scheduler"));
        RxJavaPlugins.setInitIoSchedulerHandler(Rx2Idler.create("RxJava 2.x IO Scheduler"));
    }
}