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
import com.dhis2.databinding.FragmentSearchBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.searchTrackEntity.adapters.SearchRelationshipAdapter;
import com.dhis2.usescases.searchTrackEntity.adapters.SearchTEAdapter;
import com.dhis2.usescases.searchTrackEntity.adapters.TabletSearchAdapter;
import com.evrencoskun.tableview.listener.ITableViewListener;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 16/04/2018.
 */

public class SearchLocalFragment extends FragmentGlobalAbstract implements ITableViewListener {

    private static SearchLocalFragment instance;
    private SearchTEActivity activity;
    private TabletSearchAdapter searchTEATabletAdapter;
    private SearchTEAdapter searchTEAdapter;
    private SearchRelationshipAdapter searchRelationshipAdapter;
    private boolean fromRelationship;

    public static SearchLocalFragment getInstance(ActivityGlobalAbstract context, boolean fromRelationship) {
        if (instance == null || !(instance.activity!=null && context.equals(instance.activity.getAbstracContext()))) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("fromRelationship", fromRelationship);

            instance = new SearchLocalFragment();
            instance.setArguments(bundle);
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
        FragmentSearchBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search, container, false);
        fromRelationship = getArguments().getBoolean("fromRelationship");

        if (getResources().getBoolean(R.bool.is_tablet)) {
            searchTEATabletAdapter = new TabletSearchAdapter(activity, activity.presenter, activity.metadataRepository);
            binding.tableView.setAdapter(searchTEATabletAdapter);
            binding.tableView.setTableViewListener(this);
            binding.scrollView.setVisibility(View.GONE);

        } else {
            if(fromRelationship) {
                searchRelationshipAdapter = new SearchRelationshipAdapter(activity.presenter, activity.metadataRepository, false);
                binding.scrollView.setAdapter(searchRelationshipAdapter);
            } else {
                searchTEAdapter = new SearchTEAdapter(activity.presenter, activity.metadataRepository);
                binding.scrollView.setAdapter(searchTEAdapter);
            }
            binding.tableView.setVisibility(View.GONE);
        }
        return binding.getRoot();
    }

    public void setItems(List<TrackedEntityInstanceModel> data, List<ProgramModel> programList) {

        if (getResources().getBoolean(R.bool.is_tablet)) {
            searchTEATabletAdapter.setItems(data, programList);
        } else {
            if(fromRelationship){
                searchRelationshipAdapter.setItems(data);
            } else {
                searchTEAdapter.setItems(data);
            }
        }
    }

    public void clear() {
        if (searchTEAdapter != null)
            searchTEAdapter.clear();
        if(searchRelationshipAdapter != null)
            searchRelationshipAdapter.clear();
    }

    @Override
    public void onCellClicked(@NonNull RecyclerView.ViewHolder p_jCellView, int p_nXPosition, int p_nYPosition) {
        activity.presenter.onTEIClick(searchTEATabletAdapter.getTEI(p_nYPosition).uid());
    }

    @Override
    public void onColumnHeaderClicked(@NonNull RecyclerView.ViewHolder p_jColumnHeaderView, int p_nXPosition) {

    }

    @Override
    public void onRowHeaderClicked(@NonNull RecyclerView.ViewHolder p_jRowHeaderView, int p_nYPosition) {

    }
}
