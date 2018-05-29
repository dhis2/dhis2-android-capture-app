package com.dhis2.usescases.searchTrackEntity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.data.forms.dataentry.ProgramAdapter;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.tuples.Pair;
import com.dhis2.databinding.ActivitySearchBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.searchTrackEntity.adapters.FormAdapter;
import com.dhis2.utils.NetworkUtils;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017 .
 */
@BindingMethods({
        @BindingMethod(type = FloatingActionButton.class, attribute = "app:srcCompat", method = "setImageDrawable")
})
public class SearchTEActivity extends ActivityGlobalAbstract implements SearchTEContractsModule.View {

    ActivitySearchBinding binding;
    @Inject
    SearchTEContractsModule.Presenter presenter;
    @Inject
    MetadataRepository metadataRepository;

    private String initialProgram;
    private String tEType;
    private SearchPagerAdapter pagerAdapter;
    private boolean fromRelationship = false;
    private ObservableBoolean downloadMode = new ObservableBoolean(false);
    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (pagerAdapter != null) {
                pagerAdapter.setOnline(NetworkUtils.isOnline(context));
                binding.searchTab.setVisibility(NetworkUtils.isOnline(context) ? View.VISIBLE : View.GONE);
            }
        }
    };

    //---------------------------------------------------------------------------------------------
    //region LIFECYCLE

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        ((App) getApplicationContext()).userComponent().plus(new SearchTEModule()).inject(this);

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.setPresenter(presenter);
        binding.setDownloadMode(downloadMode);
        initialProgram = getIntent().getStringExtra("PROGRAM_UID");
        tEType = getIntent().getStringExtra("TRACKED_ENTITY_UID");
        try {
            fromRelationship = getIntent().getBooleanExtra("FROM_RELATIONSHIP", false);
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }


        binding.formRecycler.setAdapter(new FormAdapter(getSupportFragmentManager(), LayoutInflater.from(this), presenter.getOrgUnits()));

    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this, tEType, initialProgram);
        registerReceiver(networkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }


    @Override
    protected void onPause() {
        presenter.onDestroy();
        unregisterReceiver(networkReceiver);
        super.onPause();
    }

    //endregion

    //-----------------------------------------------------------------------
    //region SearchForm

    @Override
    public void setForm(List<TrackedEntityAttributeModel> trackedEntityAttributeModels, @Nullable ProgramModel program) {

        //Form has been set.
        //Pager configuration based on network
        pagerAdapter = new SearchPagerAdapter(this, fromRelationship);
        pagerAdapter.setOnline(NetworkUtils.isOnline(this));
        binding.resultsPager.setAdapter(pagerAdapter);
        binding.searchTab.setVisibility(NetworkUtils.isOnline(this) ? View.VISIBLE : View.GONE);
        binding.searchTab.setupWithViewPager(binding.resultsPager);

        binding.buttonAdd.setVisibility(program == null ? View.GONE : View.VISIBLE);

        FormAdapter formAdapter = (FormAdapter) binding.formRecycler.getAdapter();
        formAdapter.setList(trackedEntityAttributeModels, program);
    }

    @NonNull
    public Flowable<RowAction> rowActionss() {
        return ((FormAdapter) binding.formRecycler.getAdapter()).asFlowableRA();
    }

    @Override
    public Flowable<Integer> onlinePage() {
        return ((SearchOnlineFragment) pagerAdapter.getItem(1)).pageAction();
    }

    //endregion

    //---------------------------------------------------------------------
    //region TEI LIST
    @Override
    public Consumer<Pair<List<TrackedEntityInstanceModel>, String>> swapListData() {
        return data -> {

            binding.progress.setVisibility(View.GONE);
            binding.objectCounter.setText(String.format(getString(R.string.search_result_text), String.valueOf(data.val0().size())));

            ((SearchLocalFragment) pagerAdapter.getItem(0)).setItems(data, presenter.getProgramList());

        };
    }

    @Override
    public void removeTei(int adapterPosition) {
        ((SearchOnlineFragment) pagerAdapter.getItem(1)).getSearchTEAdapter().removeAt(adapterPosition);
    }

    @Override
    public void handleTeiDownloads(boolean downloadMode) {
        this.downloadMode.set(downloadMode);
    }

    @Override
    public void restartOnlineFragment() {
        ((SearchOnlineFragment) pagerAdapter.getItem(1)).clear();

    }

    @Override
    public void clearList(String uid) {
        this.initialProgram = uid;
       /* ((SearchLocalFragment) pagerAdapter.getItem(0)).clear();
        if (NetworkUtils.isOnline(this))
            ((SearchOnlineFragment) pagerAdapter.getItem(1)).clear();*/
        if (uid == null)
            binding.programSpinner.setSelection(0);
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
                    binding.enrollmentButton.setVisibility(View.VISIBLE);
                } else {
                    presenter.setProgram(null);
                    binding.enrollmentButton.setVisibility(View.GONE);
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
