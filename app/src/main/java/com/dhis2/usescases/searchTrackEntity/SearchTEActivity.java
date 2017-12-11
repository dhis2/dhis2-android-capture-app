package com.dhis2.usescases.searchTrackEntity;

import android.app.DatePickerDialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.data.forms.ProgramAdapter;
import com.dhis2.databinding.ActivitySearchBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.programDetail.TrackedEntityObject;
import com.dhis2.utils.EndlessRecyclerViewScrollListener;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 02/11/2017 .
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
        binding.scrollView.addOnScrollListener(new EndlessRecyclerViewScrollListener(binding.scrollView.getLayoutManager(), 40) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                presenter.getNextPage(page);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this, getIntent().getStringExtra("TRACKED_ENTITY_UID"));
    }

    @Override
    public void setForm(List<TrackedEntityAttributeModel> trackedEntityAttributeModels, @Nullable ProgramModel program) {

        FormAdapter formAdapter;
        if (binding.formRecycler.getAdapter() == null) {
            formAdapter = new FormAdapter(presenter);
            binding.formRecycler.setAdapter(formAdapter);
        } else
            formAdapter = (FormAdapter) binding.formRecycler.getAdapter();

        formAdapter.setList(trackedEntityAttributeModels, program);
    }

    @Override
    public void showDateDialog(DatePickerDialog.OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    //Updates recycler when trackedEntityInstance list size < 4. Updates size counter
    @Override
    public void swapData(TrackedEntityObject body, List<TrackedEntityAttributeModel> attributeModels, List<ProgramModel> programModels) {
        trackedEntityInstanceList.clear();
        binding.progress.setVisibility(View.GONE);
        binding.objectCounter.setVisibility(View.VISIBLE);

        int counter = body != null ? body.getPager().total() : 0;
        binding.objectCounter.setText(String.format("%s results found", counter));

        if (searchTEAdapter == null) {
            binding.scrollView.setNestedScrollingEnabled(false);
            searchTEAdapter = new SearchTEAdapter(presenter);
            binding.scrollView.setAdapter(searchTEAdapter);
        }
        if (counter > 0 && counter < 10000) {
            trackedEntityInstanceList.addAll(body.getTrackedEntityInstances());
            searchTEAdapter.addItems(trackedEntityInstanceList, attributeModels, programModels);
        } else {
            searchTEAdapter.clear();
        }


    }


    @Override
    public void setPrograms(List<ProgramModel> programModels) {
        binding.programSpinner.setAdapter(new ProgramAdapter(this, R.layout.spinner_program_layout, R.id.spinner_text, programModels, presenter.getTrackedEntityName().displayName()));
        binding.programSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                if (pos > 0) {
                    binding.progress.setVisibility(View.VISIBLE);
                    binding.progress.setVisibility(View.GONE);
                    presenter.setProgram((ProgramModel) adapterView.getItemAtPosition(pos - 1));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void clearList() {
        if (searchTEAdapter != null)
            searchTEAdapter.clear();
    }

}
