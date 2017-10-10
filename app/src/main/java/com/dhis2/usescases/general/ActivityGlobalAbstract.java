package com.dhis2.usescases.general;

import android.content.Context;
import android.content.Intent;
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

    }

    public Context getContext() {
        return this;
    }

    public AppCompatActivity getActivity() {
        return ActivityGlobalAbstract.this;
    }

    public void startActivity(@NonNull Class<?> destination, @Nullable Bundle bundle, boolean finishCurrent, boolean finishAll, @Nullable ActivityOptionsCompat transition) {
//        NavigationController.startActivity(ActivityGlobalAbstract.this, destination, bundle, finishCurrent, finishAll, transition);
        if (finishCurrent)
            finish();
        if(transition!=null)
            ContextCompat.startActivity(this, new Intent(this, destination), transition.toBundle());
        else
            ContextCompat.startActivity(this, new Intent(this,destination),null);
    }

    public void back() {
        finish();
    }




}
