package org.dhis2.usescases.settings;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.work.WorkInfo;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.rxbinding2.widget.RxCompoundButton;
import com.jakewharton.rxbinding2.widget.RxTextView;

import org.dhis2.Bindings.ContextExtensionsKt;
import org.dhis2.Components;
import org.dhis2.R;
import org.dhis2.data.service.workManager.WorkManagerController;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.FragmentSettingsBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.NetworkUtils;
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
import static org.dhis2.utils.Constants.META_NOW;
import static org.dhis2.utils.Constants.TIME_15M;
import static org.dhis2.utils.Constants.TIME_DAILY;
import static org.dhis2.utils.Constants.TIME_HOURLY;
import static org.dhis2.utils.Constants.TIME_MANUAL;
import static org.dhis2.utils.Constants.TIME_WEEKLY;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.CONFIRM_DELETE_LOCAL_DATA;
import static org.dhis2.utils.analytics.AnalyticsConstants.CONFIRM_RESET;
import static org.dhis2.utils.analytics.AnalyticsConstants.SYNC_DATA;
import static org.dhis2.utils.analytics.AnalyticsConstants.SYNC_METADATA;
import static org.dhis2.utils.analytics.AnalyticsConstants.TYPE_SYNC;

public class SyncManagerFragment extends FragmentGlobalAbstract implements SyncManagerContracts.View {
    private final int SMS_PERMISSIONS_REQ_ID = 102;

    @Inject
    SyncManagerContracts.Presenter presenter;

    @Inject
    WorkManagerController workManagerController;

    private FragmentSettingsBinding binding;
    private SharedPreferences prefs;
    private CompositeDisposable listenerDisposable;
    private Context context;

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setNetworkEdition(NetworkUtils.isOnline(context));
        }
    };

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

        binding.smsSettings.setVisibility(ContextExtensionsKt.showSMS(context) ? View.VISIBLE : View.GONE);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        context.registerReceiver(networkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        workManagerController.getWorkInfosByTagLiveData(META_NOW).observe(this, workStatuses -> {
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
        workManagerController.getWorkInfosByTagLiveData(DATA_NOW).observe(this, workStatuses -> {
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
        context.unregisterReceiver(networkReceiver);
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
            String eventCurrent = String.valueOf(syncParameters.val1());
            String teiCurrent = String.valueOf(syncParameters.val0());
            binding.eventMaxData.setText(eventMax);
            binding.teiMaxData.setText(teiMax);
            binding.eventCurrentData.setText(eventCurrent);
            binding.teiCurrentData.setText(teiCurrent);
            binding.limitByOrgUnit.setChecked(prefs.getBoolean(Constants.LIMIT_BY_ORG_UNIT, false));
            binding.limitByProgram.setChecked(prefs.getBoolean(Constants.LIMIT_BY_PROGRAM, false));
            binding.parameterLayout.message.setText(
                    String.format(context.getString(R.string.event_tei_limits),
                            getString(R.string.tab), eventMax, getString(R.string.tab), eventCurrent,
                            getString(R.string.tab), teiMax, getString(R.string.tab), teiCurrent));
        };
    }

    @Override
    public void showInvalidGatewayError() {
        String error = getContext().getResources().getString(R.string.invalid_phone_number);
        ((TextInputLayout) binding.settingsSms.findViewById(R.id.settings_sms_receiver_layout))
                .setError(error);
    }

    @Override
    public void hideGatewayError() {
        ((TextInputLayout) binding.settingsSms.findViewById(R.id.settings_sms_receiver_layout)).setError(null);
    }

    @Override
    public void showSmsSettings(boolean enabled, String number, boolean waitForResponse, String responseSender, int timeout) {
        ((CompoundButton) binding.settingsSms.findViewById(R.id.settings_sms_switch))
                .setChecked(enabled);
        TextView gateway = binding.settingsSms.findViewById(R.id.settings_sms_receiver);
        gateway.setText(number);
        ((CompoundButton) binding.settingsSms.findViewById(R.id.settings_sms_response_wait_switch))
                .setChecked(waitForResponse);
        ((TextView) binding.settingsSms.findViewById(R.id.settings_sms_result_sender))
                .setText(responseSender);
        ((TextView) binding.settingsSms.findViewById(R.id.settings_sms_result_timeout))
                .setText(Integer.toString(timeout));
        if (!gateway.getText().toString().isEmpty()) {
            presenter.validateGatewayObservable(gateway.getText().toString());
        }
        boolean hasNetwork = NetworkUtils.isOnline(context);

        binding.settingsSms.findViewById(R.id.settings_sms_switch).setEnabled(hasNetwork);
        binding.settingsSms.findViewById(R.id.settings_sms_response_wait_switch).setEnabled(hasNetwork);
        binding.settingsSms.findViewById(R.id.settings_sms_receiver).setEnabled(hasNetwork);
        binding.settingsSms.findViewById(R.id.settings_sms_result_sender).setEnabled(hasNetwork);
        binding.settingsSms.findViewById(R.id.settings_sms_result_timeout).setEnabled(hasNetwork);

        if (NetworkUtils.isOnline(context)) {
            setSMSListeners();
        }
    }

    private void setSMSListeners() {

        listenerDisposable.add(RxTextView.textChanges(binding.settingsSms.findViewById(R.id.settings_sms_receiver))
                .skipInitialValue()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> presenter.validateGatewayObservable(data.toString()),
                        Timber::d
                ));

        listenerDisposable.add(RxCompoundButton.checkedChanges(binding.settingsSms.findViewById(R.id.settings_sms_switch))
                .debounce(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        isChecked -> {
                            if (!isChecked) {
                                presenter.smsSwitch(false);
                            } else if (NetworkUtils.isOnline(context) &&
                                    isGatewaySetAndValid() &&
                                    ContextExtensionsKt.checkSMSPermission(this, true, SMS_PERMISSIONS_REQ_ID)) {
                                presenter.smsSwitch(true);
                            }
                        }
                        ,
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
                analyticsHelper().setEvent(TYPE_SYNC, getString(R.string.data_every_fifteen_minutes), SYNC_DATA);
                time = TIME_15M;
                break;
            case R.id.data_hour:
                analyticsHelper().setEvent(TYPE_SYNC, getString(R.string.data_every_hour), SYNC_DATA);
                time = TIME_HOURLY;
                break;
            case R.id.data_day:
                analyticsHelper().setEvent(TYPE_SYNC, getString(R.string.data_every_day), SYNC_DATA);
                time = TIME_DAILY;
                break;
            case R.id.data_manual:
            default:
                analyticsHelper().setEvent(TYPE_SYNC, getString(R.string.Manual), SYNC_DATA);
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
                analyticsHelper().setEvent(TYPE_SYNC, getString(R.string.data_every_week), SYNC_METADATA);
                time = TIME_WEEKLY;
                break;
            case R.id.meta_manual:
                analyticsHelper().setEvent(TYPE_SYNC, getString(R.string.Manual), SYNC_METADATA);
                time = TIME_MANUAL;
                break;
            case R.id.meta_day:
            default:
                analyticsHelper().setEvent(TYPE_SYNC, getString(R.string.data_every_day), SYNC_METADATA);
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
                .setPositiveButton(getString(R.string.wipe_data_ok), (dialog, which) -> {
                    analyticsHelper().setEvent(CONFIRM_RESET, CLICK, CONFIRM_RESET);
                    showDeleteProgress();
                })
                .setNegativeButton(getString(R.string.wipe_data_no), (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void deleteLocalData() {
        new AlertDialog.Builder(context, R.style.CustomDialog)
                .setTitle(getString(R.string.delete_local_data))
                .setMessage(getString(R.string.delete_local_data_message))
                .setView(R.layout.warning_layout)
                .setPositiveButton(getString(R.string.action_accept), (dialog, which) -> {
                    analyticsHelper().setEvent(CONFIRM_DELETE_LOCAL_DATA, CLICK, CONFIRM_DELETE_LOCAL_DATA);
                    presenter.deleteLocalData();
                })
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

    @Override
    public void requestNoEmptySMSGateway() {
        ((TextInputLayout) binding.settingsSms.findViewById(R.id.settings_sms_receiver_layout)).setError(
                binding.getRoot().getContext().getResources().getString(R.string.sms_empty_gateway)
        );
    }

    @Override
    public void displaySMSRefreshingData() {
        Snackbar.make(
                binding.getRoot(),
                R.string.sms_downloading_data,
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void displaySMSEnabled(boolean isChecked) {
        Snackbar.make(
                binding.getRoot(),
                isChecked ? R.string.sms_enabled : R.string.sms_disabled,
                Snackbar.LENGTH_SHORT).show();
    }

    private boolean isGatewaySetAndValid() {
        String gateway =
                ((EditText) binding.settingsSms.findViewById(R.id.settings_sms_receiver)).getText().toString();
        return presenter.isGatewaySetAndValid(gateway);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == SMS_PERMISSIONS_REQ_ID && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextExtensionsKt.checkSMSPermission(this, false, SMS_PERMISSIONS_REQ_ID))
                presenter.smsSwitch(true);
        } else {
            presenter.smsSwitch(false);
        }
    }

    private void setNetworkEdition(boolean isOnline) {
        for (int i = 0; i < binding.dataRadioGroup.getChildCount(); i++) {
            binding.dataRadioGroup.getChildAt(i).setEnabled(isOnline);
        }
        for (int i = 0; i < binding.metaRadioGroup.getChildCount(); i++) {
            binding.metaRadioGroup.getChildAt(i).setEnabled(isOnline);
        }
        binding.buttonSyncData.setEnabled(isOnline);
        binding.buttonSyncData.setAlpha(isOnline ? 1.0f : 0.5f);
        binding.buttonSyncMeta.setAlpha(isOnline ? 1.0f : 0.5f);
        binding.settingsSms.findViewById(R.id.settings_sms_switch).setEnabled(isOnline);
        binding.settingsSms.findViewById(R.id.settings_sms_response_wait_switch).setEnabled(isOnline);
        binding.settingsSms.findViewById(R.id.settings_sms_receiver).setEnabled(isOnline);
        binding.settingsSms.findViewById(R.id.settings_sms_result_sender).setEnabled(isOnline);
        binding.settingsSms.findViewById(R.id.settings_sms_result_timeout).setEnabled(isOnline);
    }
}
