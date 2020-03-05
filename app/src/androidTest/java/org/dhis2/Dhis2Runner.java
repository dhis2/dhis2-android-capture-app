package org.dhis2;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.test.runner.AndroidJUnitRunner;
import org.dhis2.common.idlingresources.Rx2Idler;
import io.reactivex.plugins.RxJavaPlugins;

public class Dhis2Runner extends AndroidJUnitRunner {

    @Override
    public void onCreate(Bundle arguments) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        super.onCreate(arguments);
    }

    @Override public void onStart() {
        RxJavaPlugins.setInitComputationSchedulerHandler(Rx2Idler.create("RxJava 2.x Computation Scheduler"));
        RxJavaPlugins.setInitIoSchedulerHandler(Rx2Idler.create("RxJava 2.x IO Scheduler"));
        RxJavaPlugins.setInitNewThreadSchedulerHandler(Rx2Idler.create("RxJava 2.x New Thread Scheduler"));
        RxJavaPlugins.setInitSingleSchedulerHandler(Rx2Idler.create("RxJava 2.x Single Thread Scheduler"));
        super.onStart();
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return super.newApplication(cl, AppTest.class.getName(), context);
    }
}