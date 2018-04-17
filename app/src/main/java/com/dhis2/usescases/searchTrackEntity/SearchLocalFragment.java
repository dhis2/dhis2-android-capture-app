package com.dhis2.usescases.searchTrackEntity;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.FragmentSearchBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.searchTrackEntity.adapters.SearchTEAdapter;
import com.dhis2.usescases.searchTrackEntity.adapters.TabletSearchAdapter;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 16/04/2018.
 */

public class SearchLocalFragment extends FragmentGlobalAbstract {

    private static SearchLocalFragment instance;
    private SearchTEActivity activity;
    private TabletSearchAdapter searchTEATabletAdapter;
    private SearchTEAdapter searchTEAdapter;

    public static SearchLocalFragment getInstance() {
        if (instance == null)
            instance = new SearchLocalFragment();
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
        if (getResources().getBoolean(R.bool.is_tablet)) {
            searchTEATabletAdapter = new TabletSearchAdapter(activity, activity.presenter, activity.metadataRepository);
            binding.tableView.setAdapter(searchTEATabletAdapter);
            binding.scrollView.setVisibility(View.GONE);

        } else {
            searchTEAdapter = new SearchTEAdapter(activity.presenter, activity.metadataRepository,false);
            binding.scrollView.setAdapter(searchTEAdapter);
            binding.tableView.setVisibility(View.GONE);
        }
        return binding.getRoot();
    }

    public void setItems(List<TrackedEntityInstanceModel> data, List<ProgramModel> programList) {
        if (getResources().getBoolean(R.bool.is_tablet)) {
            searchTEATabletAdapter.setItems(data, programList);
        } else {
            searchTEAdapter.setItems(data);
        }
    }

    public void clear() {
        if (searchTEAdapter != null)
            searchTEAdapter.clear();
    }
}
