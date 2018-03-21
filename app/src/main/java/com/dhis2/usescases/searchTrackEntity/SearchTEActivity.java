package com.dhis2.usescases.searchTrackEntity;

import android.app.DatePickerDialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.data.forms.dataentry.ProgramAdapter;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ActivitySearchBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.searchTrackEntity.formHolders.FormViewHolder;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
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
    private TabletSearchAdapter searchTEATabletAdapter;

    private String initialProgram;

    //---------------------------------------------------------------------------------------------
    //region LIFECYCLE

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        ((App) getApplicationContext()).userComponent().plus(new SearchTEModule()).inject(this);

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.setPresenter(presenter);

        if (getResources().getBoolean(R.bool.is_tablet)) {
            searchTEATabletAdapter = new TabletSearchAdapter(this, presenter, metadataRepository);
            binding.tableView.setAdapter(searchTEATabletAdapter);
            binding.scrollView.setVisibility(View.GONE);

        } else {
            binding.scrollView.setNestedScrollingEnabled(false);
            searchTEAdapter = new SearchTEAdapter(presenter, metadataRepository);
            binding.scrollView.setAdapter(searchTEAdapter);
            binding.tableView.setVisibility(View.GONE);
        }


        binding.formRecycler.setAdapter(new FormAdapter(presenter));
        initialProgram = getIntent().getStringExtra("PROGRAM_UID");
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this, getIntent().getStringExtra("TRACKED_ENTITY_UID"));

    }

    @Override
    protected void onPause() {
        presenter.onDestroy();
        super.onPause();
    }

    //endregion

    //-----------------------------------------------------------------------
    //region SearchForm

    @Override
    public void setForm(List<TrackedEntityAttributeModel> trackedEntityAttributeModels, @Nullable ProgramModel program) {

        binding.buttonAdd.setVisibility(program == null ? View.GONE : View.VISIBLE);

        FormAdapter formAdapter = (FormAdapter) binding.formRecycler.getAdapter();

        formAdapter.setList(trackedEntityAttributeModels, program);
    }

    @NonNull
    @Override
    public Flowable<FormViewHolder> rowActions() {
        return ((FormAdapter) binding.formRecycler.getAdapter()).asFlowable();
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

            if (getResources().getBoolean(R.bool.is_tablet)) {
                searchTEATabletAdapter.setItems(data, presenter.getProgramList());
            } else {
                searchTEAdapter.setItems(data);
            }
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
        if(!initialProgram.isEmpty())
            setInitialProgram(programModels);
        binding.programSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                if (pos > 0) {
                    binding.progress.setVisibility(View.VISIBLE);
                    binding.objectCounter.setVisibility(View.GONE);
                    /*if (searchTEAdapter != null)
                        searchTEAdapter.clear();*/
                    presenter.setProgram((ProgramModel) adapterView.getItemAtPosition(pos - 1));
                } else {
                    binding.progress.setVisibility(View.VISIBLE);
                    binding.objectCounter.setVisibility(View.GONE);
                /*    if (searchTEAdapter != null)
                        searchTEAdapter.clear();*/
                    presenter.setProgram(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setInitialProgram(List<ProgramModel> programModels) {
        for (int i = 0; i < programModels.size(); i++) {
            if(programModels.get(i).uid().equals(initialProgram)) {
                binding.programSpinner.setSelection(i + 1);
            }
        }
    }


}
