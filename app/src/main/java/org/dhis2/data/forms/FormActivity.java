package org.dhis2.data.forms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.MenuItem;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.usescases.general.ActivityGlobalAbstract;

import static org.dhis2.utils.Preconditions.isNull;

public class FormActivity extends ActivityGlobalAbstract {
    private static final String ARGUMENTS = "formViewArguments";
    private static final String IS_ENROLLMENT = "isEnrollment";
    private Fragment fragment;

    @NonNull
    public static Intent create(@NonNull Context context,
                                @NonNull FormViewArguments formViewArguments,
                                boolean isEnrollment) {
        isNull(context, "context must not be null");
        isNull(formViewArguments, "formViewArguments must not be null");

        Intent intent = new Intent(context, FormActivity.class);
        intent.putExtra(ARGUMENTS, formViewArguments);
        intent.putExtra(IS_ENROLLMENT, isEnrollment);
        return intent;
    }

    @NonNull
    public static Intent create(@NonNull Activity activity,
                                @NonNull FormViewArguments formViewArguments,
                                boolean isEnrollment, Bundle bundle) {
        isNull(activity, "activity must not be null");
        isNull(formViewArguments, "formViewArguments must not be null");

        Intent intent = new Intent(activity, FormActivity.class);
        intent.putExtra(ARGUMENTS, formViewArguments);
        intent.putExtra(IS_ENROLLMENT, isEnrollment);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setScreenName(this.getLocalClassName());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        fragment = FormFragment.newInstance(
                getIntent().getParcelableExtra(ARGUMENTS),
                getIntent().getBooleanExtra(IS_ENROLLMENT, false),
                true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.dashboard,
                        fragment)
                .commit();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((App) getActivity().getApplicationContext())
                .releaseFormComponent();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (fragment instanceof FormView) {
            ((FormView) fragment).onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}