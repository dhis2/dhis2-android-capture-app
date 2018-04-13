package com.dhis2.usescases.syncManager;


import android.app.job.JobInfo;
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

import com.dhis2.data.service.SyncDataService;
import com.dhis2.data.service.SyncMetadataService;
import com.dhis2.databinding.FragmentSyncManagerBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class SyncManagerFragment extends FragmentGlobalAbstract implements SyncManagerContracts.View{

    private SyncManagerPresenter presenter = new SyncManagerPresenter(this);
    FirebaseJobDispatcher dispatcher;
    private FragmentSyncManagerBinding binding;
    SharedPreferences prefs;
    Job dataJob = null;
    Job metaJob = null;


    public SyncManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
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
                case 60:
               // case 180:
                    binding.radioData.check(R.id.dataMinute);
                    break;
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
            case R.id.dataMinute:
                // 1 minute
                time = 60;
                //time = 180;
                break;
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

        if (time != 0){
            prefs.edit().putInt("timeData", time).apply();
            dataJob(time);
        }
        else{
            prefs.edit().remove("timeData").apply();
            dispatcher.cancel("dataJob");
        }
    }

    private void saveTimeMeta(int i) {
        int time; //sg

        switch (i) {
            case R.id.metaDay:
                // 1 day
                //time = 86400;
                time = 180;
                break;
            case R.id.metaWeek:
                // 1 week
                time = 604800;
                break;
            default:
                time = 0;
                break;
        }

        if (time != 0){
            prefs.edit().putInt("timeMeta", time).apply();
            metaJob(time);
        }
        else{
            prefs.edit().remove("timeMeta").apply();
            dispatcher.cancel("metaJob");
        }
    }

    private void dataJob(int seconds){
        String tag = "dataJob";
        //if (dataJob != null) dispatcher.cancel(tag);
        dataJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(SyncDataService.class)
                // uniquely identifies the job
                .setTag(tag)
                // one-off job
                .setRecurring(true)
                // start between - and - seconds from now
                .setTrigger(Trigger.executionWindow(seconds, seconds+60))
                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(true)
                // don't persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
                //.setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build();


        dispatcher.mustSchedule(dataJob);

    }

    private void metaJob(int seconds){
        String tag = "metaJob";
      //  if (metaJob != null) dispatcher.cancel(tag);
        metaJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(SyncMetadataService.class)
                // uniquely identifies the job
                .setTag(tag)
                // one-off job
                .setRecurring(true)
                // start between - and - seconds from now
                .setTrigger(Trigger.executionWindow(seconds, seconds + 60))
                // don't overwrite an existing job with the same tag
                .setReplaceCurrent(true)
                // don't persist past a device reboot
                .setLifetime(Lifetime.FOREVER)
                //.setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build();

        dispatcher.mustSchedule(metaJob);

    }

}
