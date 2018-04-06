package com.dhis2.usescases.syncManager;


import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.dhis2.R;

import com.dhis2.databinding.FragmentSyncManagerBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;

/**
 * A simple {@link Fragment} subclass.
 */
public class SyncManagerFragment extends FragmentGlobalAbstract implements SyncManagerContracts.View{

    private SyncManagerPresenter presenter = new SyncManagerPresenter(this);
    private FragmentSyncManagerBinding binding;
    SharedPreferences prefs;


    public SyncManagerFragment() {
        // Required empty public constructor
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

        return binding.getRoot();
    }

    private void initRadioGroups() {
        int timeData = prefs.getInt("timeData",1);
        int timeMeta = prefs.getInt("timeMeta",1);

            switch (timeData) {
                case 60000:
                    binding.radioData.check(R.id.dataMinute);
                    break;
                case 900000:
                    binding.radioData.check(R.id.data15);
                    break;
                case 3600000:
                    binding.radioData.check(R.id.dataHour);
                    break;
                case 86400000:
                    binding.radioData.check(R.id.dataDay);
                    break;
                default:
                    binding.radioData.check(R.id.dataManual);
                    break;
            }
        switch (timeMeta) {
            case 86400000:
                binding.radioMeta.check(R.id.metaDay);
                break;
            case 604800017:
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
            case R.id.dataMinute:
                // 1 minute
                time = 60000;
                break;
            case R.id.data15:
                // 15 minutes
                time = 900000;
                break;
            case R.id.dataHour:
                // 1 hour
                time = 3600000;
                break;
            case R.id.dataDay:
                // 1 day
                time = 86400000;
                break;
            default:
                time = 0;
                break;
        }

        if (time != 0) prefs.edit().putInt("timeData", time).apply();
        else prefs.edit().remove("timeData").apply();
    }

    private void saveTimeMeta(int i) {
        int time; //ms

        switch (i) {
            case R.id.metaDay:
                // 1 day
                time = 86400000;
                break;
            case R.id.metaWeek:
                // 1 week
                time = 604800017;
                break;
            default:
                time = 0;
                break;
        }

        if (time != 0) prefs.edit().putInt("timeMeta", time).apply();
        else prefs.edit().remove("timeMeta").apply();
    }

}
