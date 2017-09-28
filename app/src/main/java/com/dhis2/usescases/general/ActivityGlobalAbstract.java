package com.dhis2.usescases.general;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Javi on 28/07/2017.
 */

public abstract class ActivityGlobalAbstract extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

    }

    public Context getContext() {
        return this;
    }

    public AppCompatActivity getActivity() {
        return ActivityGlobalAbstract.this;
    }

    public void startActivity(@NonNull Class<?> destination, @Nullable Bundle bundle, boolean finishCurrent, boolean finishAll, @Nullable ActivityOptionsCompat transition) {
//        NavigationController.startActivity(ActivityGlobalAbstract.this, destination, bundle, finishCurrent, finishAll, transition);
        if(finishCurrent)
            finish();
        ContextCompat.startActivity(this, new Intent(this, destination), transition.toBundle());
    }

    public void back() {
        finish();
    }


}
