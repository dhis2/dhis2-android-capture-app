package com.dhis2.data.forms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import com.dhis2.R;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import static com.dhis2.utils.Preconditions.isNull;

public class FormActivity extends ActivityGlobalAbstract {
    private static String ARGUMENTS = "formViewArguments";

    @NonNull
    public static Intent create(@NonNull Activity activity,
                                @NonNull FormViewArguments formViewArguments) {
        isNull(activity, "activity must not be null");
        isNull(formViewArguments, "formViewArguments must not be null");

        Intent intent = new Intent(activity, FormActivity.class);
        intent.putExtra(ARGUMENTS, formViewArguments);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboard, FormFragment.newInstance(
                        getIntent().getParcelableExtra(ARGUMENTS),true))
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}