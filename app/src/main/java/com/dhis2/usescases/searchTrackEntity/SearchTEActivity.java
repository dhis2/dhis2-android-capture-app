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
import com.dhis2.data.forms.dataentry.ProgramAdapter;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ActivitySearchBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.utils.EndlessRecyclerViewScrollListener;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

/**
 * Created by ppajuelo on 02/11/2017 .
 */

public class SearchTEActivity extends ActivityGlobalAbstract implements SearchTEContractsModule.View {

    ActivitySearchBinding binding;
    @Inject
    SearchTEContractsModule.Presenter presenter;
    @Inject
    MetadataRepository metadataRepository;

    private SearchTEAdapter searchTEAdapter;

    //---------------------------------------------------------------------------------------------
    //region LIFECYCLE

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        ((App) getApplicationContext()).userComponent().plus(new SearchTEModule()).inject(this);

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.setPresenter(presenter);
        presenter.init(this, getIntent().getStringExtra("TRACKED_ENTITY_UID"));

        binding.scrollView.addOnScrollListener(new EndlessRecyclerViewScrollListener(binding.scrollView.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
//                presenter.getNextPage(page);TODO: FIX THIS. VIEWHOLDERS DATA SWAPS INCORRECTLY
            }
        });
        binding.scrollView.setNestedScrollingEnabled(false);
        searchTEAdapter = new SearchTEAdapter(presenter, metadataRepository);
        binding.scrollView.setAdapter(searchTEAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    //endregion

    //-----------------------------------------------------------------------
    //region SearchForm

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

    //endregion

    //---------------------------------------------------------------------
    //region TEI LIST
    @Override
    public Consumer<List<TrackedEntityInstanceModel>> swapListData() {
        return data -> {
            binding.progress.setVisibility(View.GONE);
            binding.objectCounter.setVisibility(View.VISIBLE);
            binding.objectCounter.setText(String.format("%s results found", data.size()));

            searchTEAdapter.setItems(data);
        };
    }


    @Override
    public void clearList() {
        if (searchTEAdapter != null)
            searchTEAdapter.clear();
    }
    //endregion

    @Override
    public void showDateDialog(DatePickerDialog.OnDateSetListener listener) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, listener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void setPrograms(List<ProgramModel> programModels) {
        binding.programSpinner.setAdapter(new ProgramAdapter(this, R.layout.spinner_program_layout, R.id.spinner_text, programModels, presenter.getTrackedEntityName().displayName()));
        binding.programSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                if (pos > 0) {
                    binding.progress.setVisibility(View.VISIBLE);
                    binding.objectCounter.setVisibility(View.GONE);
                    if (searchTEAdapter != null)
                        searchTEAdapter.clear();
                    presenter.setProgram((ProgramModel) adapterView.getItemAtPosition(pos - 1));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


}
