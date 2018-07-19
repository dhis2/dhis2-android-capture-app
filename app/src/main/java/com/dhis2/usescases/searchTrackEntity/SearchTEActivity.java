package com.dhis2.usescases.searchTrackEntity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.databinding.BindingMethod;
import android.databinding.BindingMethods;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
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
import com.dhis2.usescases.searchTrackEntity.adapters.SearchRelationshipAdapter;
import com.dhis2.usescases.searchTrackEntity.adapters.SearchTEAdapter;
import com.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import com.dhis2.utils.EndlessRecyclerViewScrollListener;
import com.dhis2.utils.NetworkUtils;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
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
    private SearchTEAdapter searchTEAdapter;
    private SearchRelationshipAdapter searchRelationshipAdapter;

    private boolean fromRelationship = false;
    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };
    private ProgramModel program;
    private static PublishProcessor<Integer> onlinePagerProcessor;
    private PublishProcessor<Integer> offlinePagerProcessor;
    private EndlessRecyclerViewScrollListener endlessRecyclerViewScrollListener;
    //---------------------------------------------------------------------------------------------
    //region LIFECYCLE

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        ((App) getApplicationContext()).userComponent().plus(new SearchTEModule()).inject(this);

        super.onCreate(savedInstanceState);

        if (!getResources().getBoolean(R.bool.is_tablet))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        binding.setPresenter(presenter);
        initialProgram = getIntent().getStringExtra("PROGRAM_UID");
        tEType = getIntent().getStringExtra("TRACKED_ENTITY_UID");

        try {
            fromRelationship = getIntent().getBooleanExtra("FROM_RELATIONSHIP", false);
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }

        if (fromRelationship) {
            searchRelationshipAdapter = new SearchRelationshipAdapter(presenter, metadataRepository, false);
            binding.scrollView.setAdapter(searchRelationshipAdapter);
        } else {
            searchTEAdapter = new SearchTEAdapter(presenter, metadataRepository);
            binding.scrollView.setAdapter(searchTEAdapter);
        }

        binding.scrollView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        binding.formRecycler.setAdapter(new FormAdapter(getSupportFragmentManager(), LayoutInflater.from(this), presenter.getOrgUnits(), this));

        onlinePagerProcessor = PublishProcessor.create();
        offlinePagerProcessor = PublishProcessor.create();
        endlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(binding.scrollView.getLayoutManager(), 2,
                NetworkUtils.isOnline(this) ? 1 : 0) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (NetworkUtils.isOnline(SearchTEActivity.this))
                    onlinePagerProcessor.onNext(page);
                else
                    offlinePagerProcessor.onNext(page);
            }
        };
        binding.scrollView.addOnScrollListener(endlessRecyclerViewScrollListener);

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
    public void setForm(List<TrackedEntityAttributeModel> trackedEntityAttributeModels, @Nullable ProgramModel program, HashMap<String, String> queryData) {

        this.program = program;

        //TODO: refreshData for recycler

        //Form has been set.
        FormAdapter formAdapter = (FormAdapter) binding.formRecycler.getAdapter();
        formAdapter.setList(trackedEntityAttributeModels, program,queryData);
    }

    @NonNull
    public Flowable<RowAction> rowActionss() {
        return ((FormAdapter) binding.formRecycler.getAdapter()).asFlowableRA();
    }

    @Override
    public Flowable<Integer> onlinePage() {
        if (program != null)
            return onlinePagerProcessor;
        else
            return Flowable.just(-1);
    }

    @Override
    public Flowable<Integer> offlinePage() {
        return offlinePagerProcessor;
    }

    @Override
    public void clearData() {
        endlessRecyclerViewScrollListener.resetState(NetworkUtils.isOnline(this) ? 1 : 0);
        if (fromRelationship)
            searchRelationshipAdapter.clear();
        else
            searchTEAdapter.clear();
    }

    //endregion

    //---------------------------------------------------------------------
    //region TEI LIST

    @Override
    public Consumer<Pair<List<SearchTeiModel>, String>> swapTeiListData() {
        return data -> {

            if (!fromRelationship) {
                if (data.val1().isEmpty()) {
                    binding.messageContainer.setVisibility(View.GONE);
                    searchTEAdapter.setTeis(data.val0());
                } else if (searchTEAdapter.getItemCount() == 0) {
                    binding.messageContainer.setVisibility(View.VISIBLE);
                    binding.message.setText(data.val1());
                }


            } else {
                if (data.val1().isEmpty()) {
                    binding.messageContainer.setVisibility(View.GONE);
                    searchRelationshipAdapter.setItems(data.val0());
                } else if (searchTEAdapter.getItemCount() == 0) {
                    binding.messageContainer.setVisibility(View.VISIBLE);
                    binding.message.setText(data.val1());
                }
            }
        };
    }

    @Override
    public void clearList(String uid) {
        this.initialProgram = uid;

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
}
