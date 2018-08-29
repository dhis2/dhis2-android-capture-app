package com.dhis2.usescases.syncManager;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.App;
import com.dhis2.Components;
import com.dhis2.R;
import com.dhis2.data.tuples.Pair;
import com.dhis2.databinding.FragmentSyncManagerBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.utils.Constants;
import com.dhis2.utils.HelpManager;
import com.jakewharton.rxbinding2.widget.RxTextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import me.toptas.fancyshowcase.DismissListener;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
import timber.log.Timber;

import static com.dhis2.utils.Constants.TIME_15M;
import static com.dhis2.utils.Constants.TIME_DAILY;
import static com.dhis2.utils.Constants.TIME_HOURLY;
import static com.dhis2.utils.Constants.TIME_MANUAL;
import static com.dhis2.utils.Constants.TIME_WEEKLY;

/**
 * A simple {@link Fragment} subclass.
 */
public class SyncManagerFragment extends FragmentGlobalAbstract implements SyncManagerContracts.View {

    public final static String TAG_DATA = "DATA";
    public final static String TAG_META_NOW = "META_NOW";
    public final static String TAG_META = "DATA_NOW";
    public final static String TAG_DATA_NOW = "DATA_NOW";

    @Inject
    SyncManagerContracts.Presenter presenter;

    private FragmentSyncManagerBinding binding;
    private SharedPreferences prefs;
    private CompositeDisposable listenerDisposable;
    private Context context;


    public SyncManagerFragment() {
        // Required empty public constructor
    }

    private BroadcastReceiver syncReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("action_sync")) {
                if (((App) getActivity().getApplication()).isSyncing() &&
                        getAbstractActivity().progressBar.getVisibility() == View.VISIBLE) {
                    binding.buttonSyncData.setEnabled(false);
                    binding.buttonSyncMeta.setEnabled(false);
                } else {
                    binding.buttonSyncData.setEnabled(true);
                    binding.buttonSyncMeta.setEnabled(true);
                    setLastDataSyncDate();
                    setLastMetaDataSyncDate();
                }
            }
        }
    };

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
        LocalBroadcastManager.getInstance(getAbstractActivity().getApplicationContext()).registerReceiver(syncReceiver, new IntentFilter("action_sync"));

        if(((App) getActivity().getApplication()).isSyncing()){
            binding.buttonSyncData.setEnabled(false);
            binding.buttonSyncMeta.setEnabled(false);
        }

        showTutorial();

        listenerDisposable = new CompositeDisposable();

        listenerDisposable.add(RxTextView.textChanges(binding.eventMaxData).debounce(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> prefs.edit().putInt(Constants.EVENT_MAX, Integer.valueOf(data.toString())).apply(),
                        Timber::d
                ));

        listenerDisposable.add(RxTextView.textChanges(binding.teiMaxData).debounce(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> prefs.edit().putInt(Constants.TEI_MAX, Integer.valueOf(data.toString())).apply(),
                        Timber::d
                ));

        binding.limitByOrgUnit.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean(Constants.LIMIT_BY_ORG_UNIT, isChecked).apply());
        setLastDataSyncDate();
        setLastMetaDataSyncDate();
    }

    @Override
    public void onPause() {
        super.onPause();
        listenerDisposable.clear();
        LocalBroadcastManager.getInstance(getAbstractActivity().getApplicationContext()).unregisterReceiver(syncReceiver);
        presenter.disponse();
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

    public void setLastDataSyncDate() {
        if (prefs.getBoolean(Constants.LAST_DATA_SYNC_STATUS, true))
            binding.dataLastSync.setText(String.format(getString(R.string.last_data_sync_date), prefs.getString(Constants.LAST_DATA_SYNC, "-")));
        else
            binding.dataLastSync.setText(getString(R.string.sync_error_text));
    }

    public void setLastMetaDataSyncDate() {
        if (prefs.getBoolean(Constants.LAST_META_SYNC_STATUS, true))
            binding.metadataLastSync.setText(String.format(getString(R.string.last_data_sync_date), prefs.getString(Constants.LAST_META_SYNC, "-")));
        else
            binding.metadataLastSync.setText(getString(R.string.sync_error_text));
    }

    private void initRadioGroups() {
        int timeData = prefs.getInt("timeData", TIME_DAILY);
        int timeMeta = prefs.getInt("timeMeta", TIME_DAILY);

        switch (timeData) {
            case TIME_15M:
                binding.radioData.check(R.id.data15);
                break;
            case TIME_HOURLY:
                binding.radioData.check(R.id.dataHour);
                break;
            case TIME_DAILY:
            default:
                binding.radioData.check(R.id.dataDay);
                break;
        }

        switch (timeMeta) {
            case TIME_MANUAL:
                binding.radioMeta.check(R.id.metaManual);
                break;
            case TIME_WEEKLY:
                binding.radioMeta.check(R.id.metaWeek);
                break;
            case TIME_DAILY:
            default:
                binding.radioMeta.check(R.id.metaDay);
                break;
        }
    }

    private void saveTimeData(int i) {
        int time;

        switch (i) {
            case R.id.dataManual:
                // manual
                time = TIME_MANUAL;
                break;
            case R.id.data15:
                // 15 minutes
                time = TIME_15M;
                break;
            case R.id.dataHour:
                // 1 hour
                time = TIME_HOURLY;
                break;
            case R.id.dataDay:
                //daily
                time = TIME_DAILY;
                break;
            default:
                // Manual
                time = TIME_MANUAL;
                break;
        }
        prefs.edit().putInt("timeData", time).apply();
        if (time != TIME_MANUAL)
            presenter.syncData(time, "Data");
    }

    private void saveTimeMeta(int i) {
        int time;

        switch (i) {
            case R.id.metaWeek:
                // 1 week
                time = TIME_WEEKLY;
                break;
            case R.id.metaManual:
                time = TIME_MANUAL;
                break;
            case R.id.metaDay:
            default:
                // 1 day (default)
                time = TIME_DAILY;
                break;
        }

        prefs.edit().putInt("timeMeta", time).apply();
        if (time != TIME_MANUAL)
            presenter.syncMeta(time, "Meta");
    }

    @Override
    public void wipeDatabase() {
        new AlertDialog.Builder(context, R.style.CustomDialog)
                .setTitle(getString(R.string.wipe_data))
                .setMessage(getString(R.string.wipe_data_meesage))
                .setPositiveButton(getString(R.string.wipe_data_ok), (dialog, which) -> presenter.wipeDb())
                .setNegativeButton(getString(R.string.wipe_data_no), (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void showTutorial() {
        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                "com.dhis2", Context.MODE_PRIVATE);
        NestedScrollView scrollView = getAbstractActivity().findViewById(R.id.scrollView);
        new Handler().postDelayed(() -> {
            FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .focusOn(getAbstractActivity().findViewById(R.id.radioData))
                    .title(getString(R.string.tuto_settings_1))
                    .closeOnTouch(true)
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .dismissListener(new DismissListener() {
                        @Override
                        public void onDismiss(String id) {
                            if (scrollView != null) {
                                scrollView.scrollTo((int) getAbstractActivity().findViewById(R.id.radioMeta).getX(), (int) getAbstractActivity().findViewById(R.id.radioMeta).getY());
                            }
                        }

                        @Override
                        public void onSkipped(String id) {
                            // unused
                        }
                    })
                    .build();
            FancyShowCaseView tuto2 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .focusOn(getAbstractActivity().findViewById(R.id.radioMeta))
                    .title(getString(R.string.tuto_settings_2))
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .closeOnTouch(true)
                    .build();
            FancyShowCaseView tuto3 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .focusOn(getAbstractActivity().findViewById(R.id.capacityLayout))
                    .title(getString(R.string.tuto_settings_3))
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .closeOnTouch(true)
                    .build();

            FancyShowCaseView tuto4 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .focusOn(getAbstractActivity().findViewById(R.id.wipeData))
                    .title(getString(R.string.tuto_settings_4))
                    .closeOnTouch(true)
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .build();


            ArrayList<FancyShowCaseView> steps = new ArrayList<>();
            steps.add(tuto1);
            steps.add(tuto2);
            steps.add(tuto3);
            steps.add(tuto4);

            HelpManager.getInstance().setScreenHelp(getClass().getName(), steps);

            if (!prefs.getBoolean("TUTO_SETTINGS_SHOWN", false)) {
                HelpManager.getInstance().showHelp();/* getAbstractActivity().fancyShowCaseQueue.show();*/
                prefs.edit().putBoolean("TUTO_SETTINGS_SHOWN", true).apply();
            }

        }, 500);
    }
}
