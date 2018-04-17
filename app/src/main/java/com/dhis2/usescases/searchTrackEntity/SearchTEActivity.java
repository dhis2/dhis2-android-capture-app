package com.dhis2.usescases.searchTrackEntity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.data.forms.dataentry.ProgramAdapter;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ActivitySearchBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.utils.NetworkUtils;
import com.dhis2.usescases.searchTrackEntity.adapters.FormAdapter;
import com.dhis2.usescases.searchTrackEntity.adapters.SearchTEAdapter;
import com.dhis2.usescases.searchTrackEntity.adapters.TabletSearchAdapter;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

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

    private String initialProgram;
    private String tEType;
    private SearchPagerAdapter pagerAdapter;

    //---------------------------------------------------------------------------------------------
    //region LIFECYCLE

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        ((App) getApplicationContext()).userComponent().plus(new SearchTEModule()).inject(this);

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.setPresenter(presenter);

        //Pager configuration based on network
        pagerAdapter = new SearchPagerAdapter(getSupportFragmentManager());
        pagerAdapter.setOnline(NetworkUtils.isOnline(this));
        binding.resultsPager.setAdapter(pagerAdapter);
        binding.searchTab.setVisibility(NetworkUtils.isOnline(this) ? View.VISIBLE : View.GONE);
        binding.searchTab.setupWithViewPager(binding.resultsPager);

        binding.formRecycler.setAdapter(new FormAdapter(LayoutInflater.from(this)));
        initialProgram = getIntent().getStringExtra("PROGRAM_UID");
        tEType = getIntent().getStringExtra("TRACKED_ENTITY_UID");
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this, tEType, initialProgram);
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
    public Flowable<RowAction> rowActionss() {
        return ((FormAdapter) binding.formRecycler.getAdapter()).asFlowableRA();
    }

    //endregion

    //---------------------------------------------------------------------
    //region TEI LIST
    @Override
    public Consumer<List<TrackedEntityInstanceModel>> swapListData() {
        return data -> {

            binding.progress.setVisibility(View.GONE);
            binding.objectCounter.setText(String.format(getString(R.string.search_result_text), String.valueOf(data.size())));

            ((SearchLocalFragment) pagerAdapter.getItem(0)).setItems(data, presenter.getProgramList());

            if (NetworkUtils.isOnline(this))
                ((SearchOnlineFragment) pagerAdapter.getItem(1)).setItems(data, presenter.getProgramList());

        };
    }


    @Override
    public void clearList(String uid) {
        this.initialProgram = uid;
        ((SearchLocalFragment) pagerAdapter.getItem(0)).clear();
        if (NetworkUtils.isOnline(this))
            ((SearchOnlineFragment) pagerAdapter.getItem(1)).clear();
    }
    //endregion

    @Override
    public void setPrograms(List<ProgramModel> programModels) {
        binding.programSpinner.setAdapter(new ProgramAdapter(this, R.layout.spinner_program_layout, R.id.spinner_text, programModels, presenter.getTrackedEntityName().displayName()));
        if (initialProgram != null && !initialProgram.isEmpty())
            setInitialProgram(programModels);
        else
            binding.programSpinner.setSelection(0);
        binding.programSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                binding.objectCounter.setText("");
                if (pos > 0) {
                    presenter.setProgram((ProgramModel) adapterView.getItemAtPosition(pos - 1));
                } else {
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
            if (programModels.get(i).uid().equals(initialProgram)) {
                binding.programSpinner.setSelection(i + 1);
            }
        }
    }

    @Override
    public View getProgress() {
        return binding.progress;
    }
}
