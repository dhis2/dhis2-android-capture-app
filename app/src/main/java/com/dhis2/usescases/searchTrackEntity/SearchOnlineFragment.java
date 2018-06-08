package com.dhis2.usescases.searchTrackEntity;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.data.tuples.Pair;
import com.dhis2.databinding.FragmentSearchBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.searchTrackEntity.adapters.SearchTEOnlineAdapter;
import com.dhis2.usescases.searchTrackEntity.adapters.TabletSearchAdapter;
import com.dhis2.utils.EndlessRecyclerViewScrollListener;
import com.evrencoskun.tableview.listener.ITableViewListener;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModelBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

/**
 * QUADRAM. Created by ppajuelo on 16/04/2018.
 */

public class SearchOnlineFragment extends FragmentGlobalAbstract implements ITableViewListener {

    private static SearchOnlineFragment instance;
    private TabletSearchAdapter searchTEATabletAdapter;
    private SearchTEOnlineAdapter searchTEAdapter;
    private SearchTEActivity activity;
    FragmentSearchBinding binding;

    private static PublishProcessor<Integer> onlinePagerProcessor;
    private String message;

    public static SearchOnlineFragment getInstance(ActivityGlobalAbstract context, boolean fromRelationship) {
        if (instance == null) {
            instance = new SearchOnlineFragment();
            onlinePagerProcessor = PublishProcessor.create();
        }
        return instance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (SearchTEActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);

        if (getResources().getBoolean(R.bool.is_tablet)) {
            searchTEATabletAdapter = new TabletSearchAdapter(activity, activity.presenter, activity.metadataRepository);
            binding.tableView.setAdapter(searchTEATabletAdapter);
            binding.scrollView.setVisibility(View.GONE);

        } else {
            searchTEAdapter = new SearchTEOnlineAdapter(activity.presenter, activity.metadataRepository);
            binding.scrollView.setAdapter(searchTEAdapter);
            binding.tableView.setVisibility(View.GONE);
        }

        binding.scrollView.addOnScrollListener(new EndlessRecyclerViewScrollListener(binding.scrollView.getLayoutManager()) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                onlinePagerProcessor.onNext(page);
            }
        });


        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.presenter.getOnlineTrackedEntities(this);

    }

    public void setItems(Pair<List<TrackedEntityInstance>, String> mData, List<ProgramModel> programList) {
        if (mData.val1().isEmpty()) {
            message = null;
            binding.messageContainer.setVisibility(View.GONE);

            HashMap<String, List<String>> teiAttributes = new HashMap<>();
            List<TrackedEntityInstanceModel> modelData = new ArrayList<>();
            TrackedEntityInstanceModelBuilder modelBuilder = new TrackedEntityInstanceModelBuilder();
            for (TrackedEntityInstance tei : mData.val0()) {
                modelData.add(modelBuilder.buildModel(tei));
                List<String> attr = new ArrayList<>();
                if (tei.trackedEntityAttributeValues() != null)
                    for (TrackedEntityAttributeValue teiAttr : tei.trackedEntityAttributeValues()) {
                        attr.add(teiAttr.value());
                    }
                teiAttributes.put(tei.uid(), attr);
            }

            if (getResources().getBoolean(R.bool.is_tablet)) {
                searchTEATabletAdapter.setItems(modelData, programList);
            } else {
                searchTEAdapter.setItems(modelData, teiAttributes);
            }
        } else {

            binding.messageContainer.setVisibility(View.VISIBLE);
            binding.message.setText(mData.val1());

        }
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    public void clear() {
        if (searchTEAdapter != null)
            searchTEAdapter.clear();

        onlinePagerProcessor.onNext(0);
    }

    public SearchTEOnlineAdapter getSearchTEAdapter() {
        return searchTEAdapter;
    }

    public FlowableProcessor<Integer> pageAction() {
        return onlinePagerProcessor;
    }

    @Override
    public void onCellClicked(@NonNull RecyclerView.ViewHolder p_jCellView, int p_nXPosition, int p_nYPosition) {
    }

    @Override
    public void onColumnHeaderClicked(@NonNull RecyclerView.ViewHolder p_jColumnHeaderView, int p_nXPosition) {

    }

    @Override
    public void onRowHeaderClicked(@NonNull RecyclerView.ViewHolder p_jRowHeaderView, int p_nYPosition) {

    }

}
