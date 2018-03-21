package com.dhis2.usescases.syncManager;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.dhis2.R;

import com.dhis2.usescases.general.FragmentGlobalAbstract;

import io.reactivex.disposables.CompositeDisposable;

/**
 * A simple {@link Fragment} subclass.
 */
public class SyncManagerFragment extends FragmentGlobalAbstract {


    public SyncManagerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sync_manager, container, false);
        /*compositeDisposable = new CompositeDisposable();
        binding.buttonSync.setOnClickListener(view -> view.getContext().startService(new Intent(view.getContext().getApplicationContext(), SyncService.class)));*/
        return binding.getRoot();
    }

}
