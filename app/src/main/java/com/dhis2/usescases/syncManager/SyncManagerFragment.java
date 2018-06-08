package com.dhis2.usescases.syncManager;


import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.Components;
import com.dhis2.R;
import com.dhis2.data.tuples.Pair;
import com.dhis2.databinding.FragmentSyncManagerBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.utils.Constants;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class SyncManagerFragment extends FragmentGlobalAbstract implements SyncManagerContracts.View {

    @Inject
    SyncManagerContracts.Presenter presenter;

    private FragmentSyncManagerBinding binding;
    private SharedPreferences prefs;
    private CompositeDisposable listenerDisposable;
    private Context context;


    public SyncManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        ((Components) context.getApplicationContext()).userComponent()
                .plus(new SyncManagerModule(this.getContext())).inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sync_manager, container, false);

        binding.setPresenter(presenter);
        prefs = getAbstracContext().getSharedPreferences(
                "com.dhis2", Context.MODE_PRIVATE);

        initRadioGroups();

        binding.radioData.setOnCheckedChangeListener((radioGroup, i) -> saveTimeData(i));
        binding.radioMeta.setOnCheckedChangeListener((radioGroup, i) -> saveTimeMeta(i));


        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.init(this);

        listenerDisposable = new CompositeDisposable();

        listenerDisposable.add(RxTextView.textChanges(binding.eventMaxData).debounce(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            prefs.edit().putInt(Constants.EVENT_MAX, Integer.valueOf(data.toString())).apply();
                        },
                        Timber::d
                ));

        listenerDisposable.add(RxTextView.textChanges(binding.teiMaxData).debounce(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            prefs.edit().putInt(Constants.TEI_MAX, Integer.valueOf(data.toString())).apply();
                        },
                        Timber::d
                ));

        binding.limitByOrgUnit.setOnCheckedChangeListener((buttonView, isChecked) -> {

            prefs.edit().putBoolean(Constants.LIMIT_BY_ORG_UNIT, isChecked).apply();
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        listenerDisposable.dispose();
        presenter.disponse();
    }

    @Override
    public void setLastDataSyncDate(String date) {
        binding.dataLastSync.setText(String.format(getString(R.string.last_data_sync_date), date));
    }

    @Override
    public void setLastMetaDataSyncDate(String date) {
        binding.metadataLastSync.setText(String.format(getString(R.string.last_data_sync_date), date));
    }

    @Override
    public Consumer<Pair<Integer, Integer>> setSyncData() {
        return syncParameters -> {

            binding.eventMaxData.setText(String.valueOf(prefs.getInt(Constants.EVENT_MAX, Constants.EVENT_MAX_DEFAULT)));
            binding.teiMaxData.setText(String.valueOf(prefs.getInt(Constants.TEI_MAX, Constants.TEI_MAX_DEFAULT)));
            binding.eventCurrentData.setText(String.valueOf(syncParameters.val0()));
            binding.teiCurrentData.setText(String.valueOf(syncParameters.val1()));
            binding.limitByOrgUnit.setChecked(prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false));
        };
    }

    private void initRadioGroups() {
        int timeData = prefs.getInt("timeData", 1);
        int timeMeta = prefs.getInt("timeMeta", 1);

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

    private void saveTimeData(int i) {
        int time;

        switch (i) {
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
        if (time != 0) presenter.syncData(time);
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
        if (time != 0) presenter.syncMeta(time);
    }

    @Override
    public void wipeDatabase() {
        new AlertDialog.Builder(context,R.style.CustomDialog)
                .setTitle(getString(R.string.wipe_data))
                .setMessage(getString(R.string.wipe_data_meesage))
                .setPositiveButton(getString(R.string.wipe_data_ok), (dialog, which) -> {
                    presenter.wipeDb();
                })
                .setNegativeButton(getString(R.string.wipe_data_no), (dialog, which) -> dialog.dismiss())
                .show();
    }
}
