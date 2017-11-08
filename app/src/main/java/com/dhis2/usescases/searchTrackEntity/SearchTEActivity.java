package com.dhis2.usescases.searchTrackEntity;

import android.app.DatePickerDialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.PopupMenu;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivitySearchBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.programDetail.TrackedEntityObject;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class SearchTEActivity extends ActivityGlobalAbstract implements SearchTEContractsModule.View {

    ActivitySearchBinding binding;
    @Inject
    SearchTEPresenter presenter;

    List<TrackedEntityInstance> trackedEntityInstanceList = new ArrayList<>();
    private SearchTEAdapter searchTEAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        ((App) getApplicationContext()).getUserComponent().plus(new SearchTEModule()).inject(this);

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.setPresenter(presenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);

    }

    @Override
    public void setForm(List<TrackedEntityAttributeModel> trackedEntityAttributeModels) {
        FormAdapter formAdapter = new FormAdapter(presenter);
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

    //Updates recycler when trackedEntityInstance list size < 4. Updates size counter
    @Override
    public void swapData(TrackedEntityObject body) {
        trackedEntityInstanceList.clear();

        int counter = body.getPager().total();
        binding.objectCounter.setText(String.format("%s results found", counter));

        if(searchTEAdapter == null) {
            searchTEAdapter = new SearchTEAdapter(presenter);
            binding.recyclerView.setAdapter(searchTEAdapter);
        }

        if(counter > 0 && counter < 4) {
            trackedEntityInstanceList.addAll(body.getTrackedEntityInstances());
            searchTEAdapter.addItems(trackedEntityInstanceList);
        } else{
            searchTEAdapter.clear();
        }


    }

}
