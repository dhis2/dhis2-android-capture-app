package com.dhis2.usescases.syncManager;


import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.Components;
import com.dhis2.R;
import com.dhis2.data.service.SyncDataService;
import com.dhis2.data.service.SyncMetadataService;
import com.dhis2.databinding.FragmentSyncManagerBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */
public class SyncManagerFragment extends FragmentGlobalAbstract implements SyncManagerContracts.View{

    @Inject
    SyncManagerContracts.Presenter presenter;

    private FragmentSyncManagerBinding binding;
    private SharedPreferences prefs;


    public SyncManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((Components) getActivity().getApplicationContext()).userComponent()
                .plus(new SyncManagerModule(this.getContext())).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sync_manager, container, false);

        binding.setPresenter(presenter);
        prefs= getAbstracContext().getSharedPreferences(
                "com.dhis2", Context.MODE_PRIVATE);

        initRadioGroups();

        binding.radioData.setOnCheckedChangeListener((radioGroup, i) -> saveTimeData(i));
        binding.radioMeta.setOnCheckedChangeListener((radioGroup, i) -> saveTimeMeta(i));

        presenter.init(this);

        return binding.getRoot();
    }

    @Override
    public void setLastDataSyncDate(String date) {
        binding.dataLastSync.setText(String.format(getString(R.string.last_data_sync_date), date));
    }

    @Override
    public void setLastMetaDataSyncDate(String date) {
        binding.metadataLastSync.setText(String.format(getString(R.string.last_data_sync_date), date));
    }

    private void initRadioGroups() {
        int timeData = prefs.getInt("timeData",1);
        int timeMeta = prefs.getInt("timeMeta",1);

        switch (timeData) {
            case 900:
                binding.radioData.check(R.id.data15);
                break;
            case 3600:
                binding.radioData.check(R.id.dataHour);
                break;
            case 86400:
                binding.radioData.check(R.id.dataDay);
                break;
            default:
                binding.radioData.check(R.id.dataManual);
                break;
        }

        switch (timeMeta) {
            //case 86400:
            case 180:
                binding.radioMeta.check(R.id.metaDay);
                break;
            case 604800:
                binding.radioMeta.check(R.id.metaWeek);
                break;
            default:
                binding.radioMeta.check(R.id.metaManual);
                break;
        }
    }

    private void saveTimeData(int i){
        int time;

        switch (i){
            case R.id.data15:
                // 15 minutes
                time = 900;
                break;
            case R.id.dataHour:
                // 1 hour
                time = 3600;
                break;
            case R.id.dataDay:
                // 1 day
                time = 86400;
                break;
            default:
                time = 0;
                break;
        }
        prefs.edit().putInt("timeData", time).apply();
        if(time != 0) presenter.syncData(time);
    }

    private void saveTimeMeta(int i) {
        int time;

        switch (i) {
            case R.id.metaDay:
                // 1 day
                time = 86400;
                break;
            case R.id.metaWeek:
                // 1 week
                time = 604800;
                break;
            default:
                time = 0;
                break;
        }

        prefs.edit().putInt("timeMeta", time).apply();
        if(time != 0) presenter.syncMeta(time);
    }
}
