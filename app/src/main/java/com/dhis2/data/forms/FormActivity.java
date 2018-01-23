package com.dhis2.data.forms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.hisp.dhis.android.dataentry.R;

import static org.hisp.dhis.android.dataentry.commons.utils.Preconditions.isNull;

public class FormActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_form);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dashboard, FormFragment.newInstance(
                        getIntent().getParcelableExtra(ARGUMENTS)))
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