package org.dhis2.usescases.enrollment;

import android.os.Bundle;
import android.view.View;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.databinding.EnrollmentActivityBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.hisp.dhis.android.core.period.FeatureType;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import io.reactivex.functions.Consumer;

public class EnrollmentActivity extends ActivityGlobalAbstract implements EnrollmentContracts.View {

    EnrollmentActivityBinding binding;
    @Inject
    EnrollmentContracts.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        String enrollmentUid = getIntent().getStringExtra(Constants.ENROLLMENT_UID);
        EnrollmentComponent enrollmentComponent = ((App) getApplicationContext()).userComponent().plus(new EnrollmentModule(enrollmentUid));
        enrollmentComponent.inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.enrollment_activity);

        presenter.init(this);
    }

    @Override
    protected void onStop() {
        presenter.onDettach();
        super.onStop();
    }


    @Override
    public void renderEnrollmentDate(String enrollmentDateLabel, String enrollmentDate) {
        binding.enrollmentDateLayout.setHint(enrollmentDateLabel);
        binding.enrollmentDate.setText(enrollmentDate);
    }

    @Override
    public void renderIncidentDate(String incidentDateLabel, String incidentDate) {
        binding.incidentDateLayout.setHint(incidentDateLabel);
        binding.incidentDateText.setText(incidentDate);
    }

    @Override
    public void showCoordinates(FeatureType featureType) {
        binding.coordinatesView.setVisibility(featureType == FeatureType.NONE ? View.GONE : View.VISIBLE);
    }

    @Override
    public Consumer<FieldViewModel> showFields() {
        return null;
    }
}
