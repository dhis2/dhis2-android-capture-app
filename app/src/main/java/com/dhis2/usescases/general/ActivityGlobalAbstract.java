package com.dhis2.usescases.general;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.dhis2.R;

import java.util.Random;

/**
 * Created by Javi on 28/07/2017.
 */

public abstract class ActivityGlobalAbstract extends AppCompatActivity implements AbstractActivityContracts.View {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Random r = new Random();

        switch (r.nextInt(3)) {
            case 0:
                setTheme(R.style.RedTheme);
                break;
            case 1:
                setTheme(R.style.GreenTheme);
                break;
            default:
                setTheme(R.style.AppTheme);
                break;
        }

        super.onCreate(savedInstanceState);

    }

    public Context getContext() {
        return this;
    }

    public ActivityGlobalAbstract getActivity() {
        return ActivityGlobalAbstract.this;
    }

    public void startActivity(@NonNull Class<?> destination, @Nullable Bundle bundle, boolean finishCurrent, boolean finishAll, @Nullable ActivityOptionsCompat transition) {
//        NavigationController.startActivity(ActivityGlobalAbstract.this, destination, bundle, finishCurrent, finishAll, transition);
        if (finishCurrent)
            finish();
        Intent intent = new Intent(this, destination);
        if (bundle != null)
            intent.putExtras(bundle);
        if (transition != null)
            ContextCompat.startActivity(this, intent, transition.toBundle());
        else
            ContextCompat.startActivity(this, intent, null);
    }

    public ActivityGlobalAbstract getAbstracContext() {
        return this;
    }

    public ActivityGlobalAbstract getAbstractActivity() {
        return this;
    }

    public void back() {
        finish();
    }

    @Override
    public void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
