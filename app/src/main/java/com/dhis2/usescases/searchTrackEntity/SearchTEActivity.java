package com.dhis2.usescases.searchTrackEntity;

import android.app.DatePickerDialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dhis2.R;
import com.dhis2.databinding.ActivitySearchBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class SearchTEActivity extends ActivityGlobalAbstract implements SearchTEContractsModule.View {

    ActivitySearchBinding binding;
    @Inject
    SearchTEPresenter presenter;

    @Inject
    FormAdapter formAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.setPresenter(presenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init();

    }

    @Override
    public void setForm(List<TrackedEntityAttributeModel> trackedEntityAttributeModels) {

        binding.formRecycler.setAdapter(formAdapter);
        trackedEntityAttributeModels.get(0).displayShortName();
        formAdapter.setList(trackedEntityAttributeModels);

    }

    @Override
    public void showDateDialog(DatePickerDialog.OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}
