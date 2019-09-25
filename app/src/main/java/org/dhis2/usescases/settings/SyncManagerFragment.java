package org.dhis2.usescases.settings;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.jakewharton.rxbinding2.widget.RxTextView;

import org.dhis2.Components;
import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.FragmentSettingsBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.SyncUtils;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static org.dhis2.utils.Constants.DATA_NOW;
import static org.dhis2.utils.Constants.META;
import static org.dhis2.utils.Constants.META_NOW;
import static org.dhis2.utils.Constants.TIME_15M;
import static org.dhis2.utils.Constants.TIME_DAILY;
import static org.dhis2.utils.Constants.TIME_HOURLY;
import static org.dhis2.utils.Constants.TIME_MANUAL;
import static org.dhis2.utils.Constants.TIME_WEEKLY;

/**
 * A simple {@link Fragment} subclass.
 */
public class SyncManagerFragment extends FragmentGlobalAbstract implements SyncManagerContracts.View {

    @Inject
    SyncManagerContracts.Presenter presenter;

    private FragmentSettingsBinding binding;
    private SharedPreferences prefs;
    private CompositeDisposable listenerDisposable;
    private Context context;

    public SyncManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
        ((Components) context.getApplicationContext()).userComponent()
                .plus(new SyncManagerModule()).inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);

        binding.setPresenter(presenter);
        prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);

        initRadioGroups();

        binding.dataRadioGroup.setOnCheckedChangeListener((group, checkedId) -> saveTimeData(checkedId));
        binding.metaRadioGroup.setOnCheckedChangeListener((group, checkedId) -> saveTimeMeta(checkedId));

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        WorkManager.getInstance(context.getApplicationContext()).getWorkInfosByTagLiveData(META_NOW).observe(this, workStatuses -> {
            if (!workStatuses.isEmpty() && workStatuses.get(0).getState() == WorkInfo.State.RUNNING) {
                binding.syncMetaLayout.message.setTextColor(ContextCompat.getColor(context, R.color.text_black_333));
                String metaText = metaSyncSettings().concat("\n").concat(context.getString(R.string.syncing_configuration));
                binding.syncMetaLayout.message.setText(metaText);
                binding.buttonSyncMeta.setEnabled(false);
            } else {
                binding.buttonSyncMeta.setEnabled(true);
                setLastSyncDate();
                presenter.checkData();
            }
        });
        WorkManager.getInstance(context.getApplicationContext()).getWorkInfosByTagLiveData(DATA_NOW).observe(this, workStatuses -> {
            if (!workStatuses.isEmpty() && workStatuses.get(0).getState() == WorkInfo.State.RUNNING) {
                String dataText = dataSyncSetting().concat("\n").concat(context.getString(R.string.syncing_data));
                binding.syncDataLayout.message.setTextColor(ContextCompat.getColor(context, R.color.text_black_333));
                binding.syncDataLayout.message.setText(dataText);
                binding.buttonSyncData.setEnabled(false);
            } else {
                binding.buttonSyncData.setEnabled(true);
                setLastSyncDate();
                presenter.checkData();
            }
        });
        presenter.init(this);

        if (SyncUtils.isSyncRunning(context)) {
            binding.buttonSyncData.setEnabled(false);
            binding.buttonSyncMeta.setEnabled(false);
        }

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

        listenerDisposable.add(RxTextView.textChanges(binding.settingsSms.findViewById(R.id.settings_sms_receiver))
                .debounce(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> presenter.smsNumberSet(data.toString()),
                        Timber::d
                ));

        listenerDisposable.add(RxCompoundButton.checkedChanges(binding.settingsSms.findViewById(R.id.settings_sms_switch))
                .debounce(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        isChecked -> presenter.smsSwitch(isChecked),
                        Timber::d
                ));

        listenerDisposable.add(RxCompoundButton.checkedChanges(binding.settingsSms.findViewById(R.id.settings_sms_response_wait_switch))
                .debounce(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        isChecked -> presenter.smsWaitForResponse(isChecked),
                        Timber::d
                ));

        listenerDisposable.add(RxTextView.textChanges(binding.settingsSms.findViewById(R.id.settings_sms_result_sender))
                .debounce(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        number -> presenter.smsResponseSenderSet(number.toString()),
                        Timber::d
                ));

        listenerDisposable.add(RxTextView.textChanges(binding.settingsSms.findViewById(R.id.settings_sms_result_timeout))
                .debounce(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        value -> presenter.smsWaitForResponseTimeout(Integer.valueOf(value.toString())),
                        Timber::d
                ));
        if (!getResources().getBoolean(R.bool.sms_enabled)) {
            binding.settingsSms.setVisibility(View.GONE);
        }

        binding.limitByOrgUnit.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean(Constants.LIMIT_BY_ORG_UNIT, isChecked).apply());
        binding.limitByProgram.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.edit().putBoolean(Constants.LIMIT_BY_PROGRAM, isChecked).apply());

        setLastSyncDate();
    }

    @Override
    public void onPause() {
        super.onPause();
        listenerDisposable.clear();
        presenter.disponse();
    }

    @Override
    public void onStop() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(123456);
        super.onStop();
    }

    @Override
    public Consumer<Pair<Integer, Integer>> setSyncData() {
        return syncParameters -> {
            String eventMax = String.valueOf(prefs.getInt(Constants.EVENT_MAX, Constants.EVENT_MAX_DEFAULT));
            String teiMax = String.valueOf(prefs.getInt(Constants.TEI_MAX, Constants.TEI_MAX_DEFAULT));
            String eventCurrent = String.valueOf(syncParameters.val0());
            String teiCurrent = String.valueOf(syncParameters.val1());
            binding.eventMaxData.setText(eventMax);
            binding.teiMaxData.setText(teiMax);
            binding.eventCurrentData.setText(eventCurrent);
            binding.teiCurrentData.setText(teiCurrent);
            binding.limitByOrgUnit.setChecked(prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false));
            binding.limitByProgram.setChecked(prefs.getBoolean(Constants.LIMIT_BY_PROGRAM, false));
            binding.parameterLayout.message.setText(
                    String.format("Events:%smax:%s%scurrent:%s\nTEI:%smax:%s%scurrent:%s",
                            getString(R.string.tab), eventMax, getString(R.string.tab), eventCurrent,
                            getString(R.string.tab), teiMax, getString(R.string.tab), teiCurrent));
        };
    }

    @Override
    public void showSmsSettings(boolean enabled, String number, boolean waitForResponse, String responseSender, int timeout) {
        ((CompoundButton) binding.settingsSms.findViewById(R.id.settings_sms_switch))
                .setChecked(enabled);
        ((TextView) binding.settingsSms.findViewById(R.id.settings_sms_receiver))
                .setText(number);
        ((CompoundButton) binding.settingsSms.findViewById(R.id.settings_sms_response_wait_switch))
                .setChecked(waitForResponse);
        ((TextView) binding.settingsSms.findViewById(R.id.settings_sms_result_sender))
                .setText(responseSender);
        ((TextView) binding.settingsSms.findViewById(R.id.settings_sms_result_timeout))
                .setText(Integer.toString(timeout));
    }

    private void setLastSyncDate() {
        boolean dataStatus = prefs.getBoolean(Constants.LAST_DATA_SYNC_STATUS, true);
        boolean metaStatus = prefs.getBoolean(Constants.LAST_META_SYNC_STATUS, true);

        if (dataStatus) {
            String dataText = dataSyncSetting().concat("\n").concat(String.format(getString(R.string.last_data_sync_date), prefs.getString(Constants.LAST_DATA_SYNC, "-")));
            binding.syncDataLayout.message.setText(dataText);
            binding.syncDataLayout.message.setTextColor(ContextCompat.getColor(context, R.color.text_black_333));
        } else {
            String dataText = dataSyncSetting().concat("\n").concat(getString(R.string.sync_error_text));
            binding.syncDataLayout.message.setText(dataText);
        }

        if (presenter.dataHasErrors()) {
            String src = dataSyncSetting().concat("\n").concat(getString(R.string.data_sync_error));
            SpannableString str = new SpannableString(src);
            int wIndex = src.indexOf('@');
            int eIndex = src.indexOf('$');
            str.setSpan(new ImageSpan(getContext(), R.drawable.ic_sync_warning), wIndex, wIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new ImageSpan(getContext(), R.drawable.ic_sync_problem_red), eIndex, eIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.syncDataLayout.message.setText(str);
            binding.syncDataLayout.message.setTextColor(ContextCompat.getColor(getContext(), R.color.red_060));

        } else if (presenter.dataHasWarnings()) {
            String src = dataSyncSetting().concat("\n").concat(getString(R.string.data_sync_warning));
            SpannableString str = new SpannableString(src);
            int wIndex = src.indexOf('@');
            str.setSpan(new ImageSpan(getContext(), R.drawable.ic_sync_warning), wIndex, wIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.syncDataLayout.message.setText(str);
            binding.syncDataLayout.message.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryOrange));
        }

        if (metaStatus) {
            String metaText = metaSyncSettings().concat("\n").concat(String.format(getString(R.string.last_data_sync_date), prefs.getString(Constants.LAST_META_SYNC, "-")));
            binding.syncMetaLayout.message.setText(metaText);
            binding.syncMetaLayout.message.setTextColor(ContextCompat.getColor(context, R.color.text_black_333));
        } else {
            String metaText = metaSyncSettings().concat("\n").concat(getString(R.string.metadata_sync_error));
            binding.syncMetaLayout.message.setText(metaText);
            binding.syncMetaLayout.message.setTextColor(ContextCompat.getColor(context, R.color.red_060));
        }

    }

    private void initRadioGroups() {
        int timeData = prefs.getInt("timeData", TIME_DAILY);
        int timeMeta = prefs.getInt("timeMeta", TIME_DAILY);

        switch (timeData) {
            case TIME_15M:
                binding.dataRadioGroup.check(R.id.data_quarter);
                break;
            case TIME_HOURLY:
                binding.dataRadioGroup.check(R.id.data_hour);
                break;
            case TIME_MANUAL:
                binding.buttonSyncData.setVisibility(View.VISIBLE);
                binding.dataRadioGroup.check(R.id.data_manual);
                break;
            case TIME_DAILY:
            default:
                binding.dataRadioGroup.check(R.id.data_day);
                break;
        }

        switch (timeMeta) {
            case TIME_MANUAL:
                binding.buttonSyncMeta.setVisibility(View.VISIBLE);
                binding.metaRadioGroup.check(R.id.meta_manual);
                break;
            case TIME_WEEKLY:
                binding.metaRadioGroup.check(R.id.meta_weekly);
                break;
            case TIME_DAILY:
            default:
                binding.metaRadioGroup.check(R.id.meta_day);
                break;
        }
    }

    private void saveTimeData(int checkedId) {
        int time;

        switch (checkedId) {
            case R.id.data_quarter:
                time = TIME_15M;
                break;
            case R.id.data_hour:
                time = TIME_HOURLY;
                break;
            case R.id.data_day:
                time = TIME_DAILY;
                break;
            case R.id.data_manual:
            default:
                time = TIME_MANUAL;
                break;
        }
        prefs.edit().putInt(Constants.TIME_DATA, time).apply();
        if (time != TIME_MANUAL) {
            binding.buttonSyncData.setVisibility(View.GONE);
            presenter.syncData(time, Constants.DATA);
        } else {
            binding.buttonSyncData.setVisibility(View.VISIBLE);
            presenter.cancelPendingWork(Constants.DATA);
        }
    }

    private void saveTimeMeta(int i) {
        int time;

        switch (i) {
            case R.id.meta_weekly:
                time = TIME_WEEKLY;
                break;
            case R.id.meta_manual:
                time = TIME_MANUAL;
                break;
            case R.id.meta_day:
            default:
                time = TIME_DAILY;
                break;
        }

        prefs.edit().putInt(Constants.TIME_META, time).apply();
        if (time != TIME_MANUAL) {
            binding.buttonSyncMeta.setVisibility(View.GONE);
            presenter.syncMeta(time, Constants.META);
        } else {
            binding.buttonSyncMeta.setVisibility(View.VISIBLE);
            presenter.cancelPendingWork(Constants.META);
        }
    }

    @Override
    public void wipeDatabase() {
        new AlertDialog.Builder(context, R.style.CustomDialog)
                .setTitle(getString(R.string.wipe_data))
                .setMessage(getString(R.string.wipe_data_meesage))
                .setView(R.layout.warning_layout)
                .setPositiveButton(getString(R.string.wipe_data_ok), (dialog, which) -> showDeleteProgress())
                .setNegativeButton(getString(R.string.wipe_data_no), (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void deleteLocalData() {
        new AlertDialog.Builder(context, R.style.CustomDialog)
                .setTitle(getString(R.string.delete_local_data))
                .setMessage(getString(R.string.delete_local_data_message))
                .setView(R.layout.warning_layout)
                .setPositiveButton(getString(R.string.action_accept), (dialog, which) -> presenter.deleteLocalData())
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showDeleteProgress() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel("wipe_notification", "Restart", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, "wipe_notification")
                        .setSmallIcon(R.drawable.ic_sync)
                        .setContentTitle(getString(R.string.wipe_data))
                        .setContentText(getString(R.string.please_wait))
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(123456, notificationBuilder.build());
        presenter.wipeDb();

    }

    @Override
    public void showTutorial() {
        if (isAdded() && getContext() != null)
            new Handler().postDelayed(() -> {
                if (getAbstractActivity() != null) {
                    HelpManager.getInstance().setScroll(getAbstractActivity().findViewById(R.id.scrollView));
                    HelpManager.getInstance().show(getAbstractActivity(), HelpManager.TutorialName.SETTINGS_FRAGMENT, null);
                }
            }, 500);
    }

    @Override
    public void showSyncErrors(List<TrackerImportConflict> data) {
        new ErrorDialog().setData(data).show(getChildFragmentManager().beginTransaction(), ErrorDialog.TAG);
    }

    @Override
    public void showLocalDataDeleted(boolean error) {

        if (!error) {
            binding.eventCurrentData.setText(String.valueOf(0));
            binding.teiCurrentData.setText(String.valueOf(0));
        }

        Snackbar deleteDataSnack = Snackbar.make(binding.getRoot(),
                error ? R.string.delete_local_data_error : R.string.delete_local_data_done,
                Snackbar.LENGTH_SHORT);
        deleteDataSnack.show();
    }

    @Override
    public void syncData() {
        String dataText = dataSyncSetting().concat("\n").concat(context.getString(R.string.syncing_data));
        binding.syncDataLayout.message.setText(dataText);
    }

    @Override
    public void syncMeta() {
        String metaText = metaSyncSettings().concat("\n").concat(getString(R.string.syncing_configuration));
        binding.syncMetaLayout.message.setText(metaText);
    }

    @Override
    public void openItem(int settingsItem) {
        binding.dataRadioGroup.setVisibility(View.GONE);
        binding.metaRadioGroup.setVisibility(View.GONE);
        binding.parameterData.setVisibility(View.GONE);
        binding.deleteDataButton.setVisibility(View.GONE);
        binding.resetButton.setVisibility(View.GONE);
        binding.smsContent.setVisibility(View.GONE);

        switch (settingsItem) {
            case 0:
                binding.dataRadioGroup.setVisibility(View.VISIBLE);
                break;
            case 1:
                binding.metaRadioGroup.setVisibility(View.VISIBLE);
                break;
            case 2:
                binding.parameterData.setVisibility(View.VISIBLE);
                break;
            case 5:
                binding.deleteDataButton.setVisibility(View.VISIBLE);
                break;
            case 6:
                binding.resetButton.setVisibility(View.VISIBLE);
                break;
            case 7:
                binding.smsContent.setVisibility(View.VISIBLE);
                break;
        }
    }

    private String dataSyncSetting() {
        int timeData = prefs.getInt("timeData", TIME_DAILY);
        String setting;
        switch (timeData) {
            case TIME_15M:
                setting = getString(R.string.data_every_fifteen_minutes);
                break;
            case TIME_HOURLY:
                setting = getString(R.string.data_every_hour);
                break;
            case TIME_MANUAL:
                setting = getString(R.string.Manual);
                break;
            case TIME_DAILY:
            default:
                setting = getString(R.string.data_every_day);
                break;
        }

        return String.format(context.getString(R.string.sync_setting), setting);
    }

    private String metaSyncSettings() {
        int timeMeta = prefs.getInt("timeMeta", TIME_DAILY);
        String setting;
        switch (timeMeta) {
            case TIME_MANUAL:
                setting = getString(R.string.Manual);
                break;
            case TIME_WEEKLY:
                setting = getString(R.string.data_every_week);
                break;
            case TIME_DAILY:
            default:
                setting = getString(R.string.data_every_day);
                break;
        }

        return String.format(context.getString(R.string.sync_setting), setting);
    }
}
