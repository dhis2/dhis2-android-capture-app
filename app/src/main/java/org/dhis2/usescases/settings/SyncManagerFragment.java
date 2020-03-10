package org.dhis2.usescases.settings;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.work.WorkInfo;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Components;
import org.dhis2.R;
import org.dhis2.data.service.workManager.WorkManagerController;
import org.dhis2.databinding.FragmentSettingsBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.settings.models.DataSettingsViewModel;
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel;
import org.dhis2.usescases.settings.models.ReservedValueSettingsViewModel;
import org.dhis2.usescases.settings.models.SMSSettingsViewModel;
import org.dhis2.usescases.settings.models.SyncParametersViewModel;
import org.dhis2.usescases.settings_program.SettingsProgramActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.NetworkUtils;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.settings.LimitScope;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import static org.dhis2.Bindings.SettingExtensionsKt.EVERY_12_HOUR;
import static org.dhis2.Bindings.SettingExtensionsKt.EVERY_15_MIN;
import static org.dhis2.Bindings.SettingExtensionsKt.EVERY_24_HOUR;
import static org.dhis2.Bindings.SettingExtensionsKt.EVERY_6_HOUR;
import static org.dhis2.Bindings.SettingExtensionsKt.EVERY_7_DAYS;
import static org.dhis2.Bindings.SettingExtensionsKt.EVERY_HOUR;
import static org.dhis2.utils.Constants.DATA_NOW;
import static org.dhis2.utils.Constants.META_NOW;
import static org.dhis2.utils.Constants.TIME_MANUAL;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.CONFIRM_DELETE_LOCAL_DATA;
import static org.dhis2.utils.analytics.AnalyticsConstants.CONFIRM_RESET;

public class SyncManagerFragment extends FragmentGlobalAbstract implements SyncManagerContracts.View {

    @Inject
    SyncManagerContracts.Presenter presenter;

    @Inject
    WorkManagerController workManagerController;

    private FragmentSettingsBinding binding;
    private Context context;

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setNetworkEdition(NetworkUtils.isOnline(context));
        }
    };
    private boolean dataInit;
    private boolean metadataInit;
    private boolean scopeLimitInit;

    public SyncManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
        ((Components) context.getApplicationContext()).userComponent()
                .plus(new SyncManagerModule(this)).inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);

        binding.setPresenter(presenter);

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
                presenter.checkData();
            }
        });
        presenter.init();

        if (!getResources().getBoolean(R.bool.sms_enabled)) {
            binding.settingsSms.getRoot().setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        context.unregisterReceiver(networkReceiver);
        presenter.dispose();
    }

    @Override
    public void onStop() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(123456);
        super.onStop();
    }

    @Override
    public void showInvalidGatewayError() {
        String error = context.getResources().getString(R.string.invalid_phone_number);
        binding.settingsSms.settingsSmsReceiverLayout.setError(error);
    }

    @Override
    public void hideGatewayError() {
        binding.settingsSms.settingsSmsReceiverLayout.setError(null);
    }

    private void saveTimeData(int time) {
        if (time != TIME_MANUAL) {
            presenter.syncData(time, Constants.DATA);
        } else {
            presenter.cancelPendingWork(Constants.DATA);
        }
    }

    private void saveTimeMeta(int time) {
        if (time != TIME_MANUAL) {
            presenter.syncMeta(time, Constants.META);
        } else {
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
        if (isAdded() && context != null)
            new Handler().postDelayed(() -> {
                if (getAbstractActivity() != null) {
                    HelpManager.getInstance().setScroll(getAbstractActivity().findViewById(R.id.scrollView));
                    HelpManager.getInstance().show(getAbstractActivity(), HelpManager.TutorialName.SETTINGS_FRAGMENT, null);
                }
            }, 500);
    }

    @Override
    public void showSyncErrors(List<D2Error> data) {
        new ErrorDialog().setData(data).show(getChildFragmentManager().beginTransaction(), ErrorDialog.TAG);
    }

    @Override
    public void showLocalDataDeleted(boolean error) {

        if (!error) {
            binding.eventsEditText.setText(String.valueOf(0));
            binding.teiEditText.setText(String.valueOf(0));
        }

        Snackbar deleteDataSnack = Snackbar.make(binding.getRoot(),
                error ? R.string.delete_local_data_error : R.string.delete_local_data_done,
                BaseTransientBottomBar.LENGTH_SHORT);
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
    public void openItem(SettingItem settingsItem) {
        binding.syncDataActions.setVisibility(View.GONE);
        binding.syncMetadataActions.setVisibility(View.GONE);
        binding.parameterData.setVisibility(View.GONE);
        binding.reservedValuesActions.setVisibility(View.GONE);
        binding.deleteDataButton.setVisibility(View.GONE);
        binding.resetButton.setVisibility(View.GONE);
        binding.smsContent.setVisibility(View.GONE);

        switch (settingsItem) {
            case DATA_SYNC:
                binding.syncDataActions.setVisibility(View.VISIBLE);
                break;
            case META_SYNC:
                binding.syncMetadataActions.setVisibility(View.VISIBLE);
                break;
            case SYNC_PARAMETERS:
                binding.parameterData.setVisibility(View.VISIBLE);
                break;
            case RESERVED_VALUES:
                binding.reservedValuesActions.setVisibility(View.VISIBLE);
                break;
            case DELETE_LOCAL_DATA:
                binding.deleteDataButton.setVisibility(View.VISIBLE);
                break;
            case RESET_APP:
                binding.resetButton.setVisibility(View.VISIBLE);
                break;
            case SMS:
                binding.smsContent.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private String dataSyncSetting() {
        int timeData = presenter.getDataPeriodSetting();
        String setting;
        switch (timeData) {
            case EVERY_15_MIN:
                setting = getString(R.string.fifteen_minute);
                break;
            case EVERY_HOUR:
                setting = getString(R.string.a_hour);
                break;
            case EVERY_6_HOUR:
                setting = getString(R.string.every_6_hours);
                break;
            case EVERY_12_HOUR:
                setting = getString(R.string.every_12_hours);
                break;
            case TIME_MANUAL:
                setting = getString(R.string.Manual);
                break;
            case EVERY_24_HOUR:
            default:
                setting = getString(R.string.a_day);
                break;
        }

        return String.format(context.getString(R.string.settings_sync_period).concat(": %s"), setting);
    }

    private String metaSyncSettings() {
        int timeMeta = presenter.getMetadataPeriodSetting();
        String setting;
        switch (timeMeta) {
            case EVERY_12_HOUR:
                setting = getString(R.string.every_12_hours);
                break;
            case EVERY_7_DAYS:
                setting = getString(R.string.a_week);
                break;
            case TIME_MANUAL:
                setting = getString(R.string.Manual);
                break;
            case EVERY_24_HOUR:
            default:
                setting = getString(R.string.a_day);
                break;
        }

        return String.format(context.getString(R.string.settings_sync_period).concat(": %s"), setting);
    }

    @Override
    public void setDataSettings(DataSettingsViewModel dataSettings) {
        dataInit = false;
        binding.dataPeriods.setOnItemSelectedListener(null);
        if (!dataSettings.getSyncHasErrors()) {
            String dataText = dataSyncSetting()
                    .concat("\n")
                    .concat(String.format(getString(R.string.last_data_sync_date), dataSettings.getLastDataSync()));
            binding.syncDataLayout.message.setText(dataText);
            binding.syncDataLayout.message.setTextColor(ContextCompat.getColor(context, R.color.text_black_333));
        } else {
            String dataText = dataSyncSetting()
                    .concat("\n")
                    .concat(getString(R.string.sync_error_text));
            binding.syncDataLayout.message.setText(dataText);
        }

        if (dataSettings.getDataHasErrors()) {
            String src = dataSyncSetting()
                    .concat("\n")
                    .concat(getString(R.string.data_sync_error));
            SpannableString str = new SpannableString(src);
            int wIndex = src.indexOf('@');
            int eIndex = src.indexOf('$');
            str.setSpan(new ImageSpan(context, R.drawable.ic_sync_warning), wIndex, wIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new ImageSpan(context, R.drawable.ic_sync_problem_red), eIndex, eIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.syncDataLayout.message.setText(str);
            binding.syncDataLayout.message.setTextColor(ContextCompat.getColor(context, R.color.red_060));

        } else if (dataSettings.getDataHasWarnings()) {
            String src = dataSyncSetting().concat("\n").concat(getString(R.string.data_sync_warning));
            SpannableString str = new SpannableString(src);
            int wIndex = src.indexOf('@');
            str.setSpan(new ImageSpan(context, R.drawable.ic_sync_warning), wIndex, wIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.syncDataLayout.message.setText(str);
            binding.syncDataLayout.message.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryOrange));
        }

        if (dataSettings.getCanEdit()) {
            binding.dataPeriodsHint.setVisibility(View.VISIBLE);
            binding.dataPeriods.setVisibility(View.VISIBLE);
            binding.dataPeriodsNoEdition.setVisibility(View.GONE);
        } else {
            binding.dataPeriodsHint.setVisibility(View.GONE);
            binding.dataPeriods.setVisibility(View.GONE);
            binding.dataPeriodsNoEdition.setVisibility(View.VISIBLE);
        }

        binding.dataPeriods.setEnabled(dataSettings.getCanEdit());
        binding.dataPeriods.setAdapter(new ArrayAdapter<>(context, R.layout.spinner_settings_item,
                context.getResources().getStringArray(R.array.data_sync_periods)));

        switch (dataSettings.getDataSyncPeriod()) {
            case EVERY_15_MIN:
                binding.dataPeriods.setSelection(0);
                break;
            case EVERY_HOUR:
                binding.dataPeriods.setSelection(1);
                break;
            case EVERY_6_HOUR:
                binding.dataPeriods.setSelection(2);
                break;
            case EVERY_12_HOUR:
                binding.dataPeriods.setSelection(3);
                break;
            case TIME_MANUAL:
                binding.dataPeriods.setSelection(5);
                break;
            case EVERY_24_HOUR:
            default:
                binding.dataPeriods.setSelection(4);
                break;
        }
        binding.dataPeriods.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (dataInit) {
                    switch (position) {
                        case 0:
                            saveTimeData(EVERY_15_MIN);
                            break;
                        case 1:
                            saveTimeData(EVERY_HOUR);
                            break;
                        case 2:
                            saveTimeData(EVERY_6_HOUR);
                            break;
                        case 3:
                            saveTimeData(EVERY_12_HOUR);
                            break;
                        case 4:
                            saveTimeData(EVERY_24_HOUR);
                            break;
                        case 5:
                            saveTimeData(TIME_MANUAL);
                            break;
                        default:
                            break;
                    }
                } else {
                    dataInit = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                /*do nothing*/
            }
        });
    }

    @Override
    public void setMetadataSettings(MetadataSettingsViewModel metadataSettings) {
        metadataInit = false;
        binding.metadataPeriods.setOnItemSelectedListener(null);

        if (!metadataSettings.getHasErrors()) {
            String metaText = metaSyncSettings().concat("\n").concat(String.format(getString(R.string.last_data_sync_date), metadataSettings.getLastMetadataSync()));
            binding.syncMetaLayout.message.setText(metaText);
            binding.syncMetaLayout.message.setTextColor(ContextCompat.getColor(context, R.color.text_black_333));
        } else {
            String metaText = metaSyncSettings().concat("\n").concat(getString(R.string.metadata_sync_error));
            binding.syncMetaLayout.message.setText(metaText);
            binding.syncMetaLayout.message.setTextColor(ContextCompat.getColor(context, R.color.red_060));
        }

        if (metadataSettings.getCanEdit()) {
            binding.metadataPeriods.setVisibility(View.VISIBLE);
            binding.metadataPeriods.setVisibility(View.VISIBLE);
            binding.metaPeriodsNoEdition.setVisibility(View.GONE);
        } else {
            binding.metadataPeriods.setVisibility(View.GONE);
            binding.metadataPeriods.setVisibility(View.GONE);
            binding.metaPeriodsNoEdition.setVisibility(View.VISIBLE);
        }

        binding.metadataPeriods.setEnabled(metadataSettings.getCanEdit());
        binding.metadataPeriods.setAdapter(new ArrayAdapter<>(context, R.layout.spinner_settings_item,
                context.getResources().getStringArray(R.array.metadata_sync_periods)));
        switch (metadataSettings.getMetadataSyncPeriod()) {
            case EVERY_12_HOUR:
                binding.metadataPeriods.setSelection(0);
                break;
            case EVERY_7_DAYS:
                binding.metadataPeriods.setSelection(2);
                break;
            case TIME_MANUAL:
                binding.metadataPeriods.setSelection(3);
                break;
            case EVERY_24_HOUR:
            default:
                binding.metadataPeriods.setSelection(1);
                break;
        }
        binding.metadataPeriods.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (metadataInit) {
                    switch (position) {
                        case 0:
                            saveTimeMeta(EVERY_12_HOUR);
                            break;
                        case 1:
                            saveTimeMeta(EVERY_24_HOUR);
                            break;
                        case 2:
                            saveTimeMeta(EVERY_7_DAYS);
                            break;
                        case 3:
                            saveTimeMeta(TIME_MANUAL);
                            break;
                        default:
                            break;
                    }
                } else {
                    metadataInit = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                /*do nothing*/
            }
        });
    }

    @Override
    public void setParameterSettings(SyncParametersViewModel parameterSettings) {
        scopeLimitInit = false;
        String eventMax = String.valueOf(parameterSettings.getNumberOfEventsToDownload());
        String teiMax = String.valueOf(parameterSettings.getNumberOfTeiToDownload());
        String eventCurrent = String.valueOf(parameterSettings.getCurrentEventCount());
        String teiCurrent = String.valueOf(parameterSettings.getCurrentTeiCount());
        binding.eventsEditText.setText(eventMax);
        binding.teiEditText.setText(teiMax);

        String limitScope;
        switch (parameterSettings.getLimitScope()) {
            case PER_PROGRAM:
                binding.downloadLimitScope.setSelection(2);
                limitScope = getString(R.string.settings_limit_program);
                break;
            case PER_ORG_UNIT:
                binding.downloadLimitScope.setSelection(1);
                limitScope = getString(R.string.settings_limit_ou);
                break;
            case PER_OU_AND_PROGRAM:
                binding.downloadLimitScope.setSelection(3);
                limitScope = getString(R.string.settings_limit_ou_program);
                break;
            default:
                binding.downloadLimitScope.setSelection(0);
                limitScope = getString(R.string.settings_limit_globally);
                break;
        }

        if (parameterSettings.getHasSpecificProgramSettings() > 0) {
            binding.specificSettingsText.setVisibility(View.VISIBLE);
            binding.specificSettingsButton.setVisibility(View.VISIBLE);
            String quantityString = context.getResources()
                    .getQuantityString(R.plurals.settings_specific_programs,
                            parameterSettings.getHasSpecificProgramSettings());
            SpannableString str = new SpannableString(String.format(quantityString,
                    parameterSettings.getHasSpecificProgramSettings()));
            int indexOfNumber = str.toString().indexOf(String.valueOf(parameterSettings.getHasSpecificProgramSettings()));
            str.setSpan(new ForegroundColorSpan(ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.PRIMARY)),
                    indexOfNumber,
                    indexOfNumber + 1,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            binding.specificSettingsText.setText(str);
            binding.specificSettingsButton.setOnClickListener(view ->
                    startActivity(SettingsProgramActivity.Companion.getIntentActivity(context)));
        } else {
            binding.specificSettingsText.setVisibility(View.GONE);
            binding.specificSettingsButton.setVisibility(View.GONE);
        }

        binding.downloadLimitScope.setAdapter(new ArrayAdapter<>(context, R.layout.spinner_settings_item,
                context.getResources().getStringArray(R.array.download_limit_scope)));

        if (parameterSettings.getLimitScopeIsEditable()) {
            binding.downloadLimitScopeHint.setVisibility(View.VISIBLE);
            binding.eventsEditText.setVisibility(View.VISIBLE);
            binding.teiEditText.setVisibility(View.VISIBLE);
            binding.downloadLimitScope.setVisibility(View.VISIBLE);
            binding.parametersNoEdition.setVisibility(View.GONE);
            setUpSyncParameterListeners();
        } else {
            binding.downloadLimitScopeHint.setVisibility(View.GONE);
            binding.eventsEditText.setVisibility(View.GONE);
            binding.teiEditText.setVisibility(View.GONE);
            binding.downloadLimitScope.setVisibility(View.GONE);
            binding.parametersNoEdition.setVisibility(View.VISIBLE);
        }
        binding.eventsEditText.setEnabled(parameterSettings.getEventNumberIsEditable());
        binding.teiEditText.setEnabled(parameterSettings.getTeiNumberIsEditable());
        binding.downloadLimitScope.setEnabled(parameterSettings.getLimitScopeIsEditable());

        binding.parameterLayout.message.setText(
                String.format(context.getString(R.string.event_tei_limits_v2),
                        limitScope,
                        eventCurrent, eventMax, teiCurrent, teiMax));
    }

    private void setUpSyncParameterListeners() {
        binding.downloadLimitScope.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (scopeLimitInit) {
                    switch (position) {
                        case 0:
                        default:
                            presenter.saveLimitScope(LimitScope.GLOBAL);
                            break;
                        case 1:
                            presenter.saveLimitScope(LimitScope.PER_ORG_UNIT);
                            break;
                        case 2:
                            presenter.saveLimitScope(LimitScope.PER_PROGRAM);
                            break;
                        case 3:
                            presenter.saveLimitScope(LimitScope.PER_OU_AND_PROGRAM);
                            break;
                    }
                } else {
                    scopeLimitInit = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                /*do nothing*/
            }
        });

        binding.eventsEditText.setOnEditorActionListener((view, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (!binding.eventsEditText.getText().toString().isEmpty()) {
                    presenter.saveEventMaxCount(Integer.valueOf(binding.eventsEditText.getText().toString()));
                }
                ExtensionsKt.closeKeyboard(view);
                return true;
            } else {
                return false;
            }
        });

        binding.teiEditText.setOnEditorActionListener((view, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (!binding.teiEditText.getText().toString().isEmpty()) {
                    presenter.saveTeiMaxCount(Integer.valueOf(binding.teiEditText.getText().toString()));
                    ExtensionsKt.closeKeyboard(view);
                }
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    public void setSMSSettings(SMSSettingsViewModel smsSettingsViewModel) {
        binding.settingsSms.settingsSmsSwitch.setChecked(smsSettingsViewModel.isEnabled());
        binding.settingsSms.settingsSmsReceiver.setText(smsSettingsViewModel.getGatewayNumber());
        binding.settingsSms.settingsSmsResponseWaitSwitch.setChecked(smsSettingsViewModel.getWaitingForResponse());
        binding.settingsSms.settingsSmsResultSender.setText(smsSettingsViewModel.getResponseNumber());
        binding.settingsSms.settingsSmsResultTimeout.setText(String.format("%s", smsSettingsViewModel.getResponseTimeout()));
        if (!binding.settingsSms.settingsSmsReceiver.getText().toString().isEmpty()) {
            presenter.validateGatewayObservable(binding.settingsSms.settingsSmsReceiver.getText().toString());
        }
        boolean hasNetwork = NetworkUtils.isOnline(context);

        binding.settingsSms.settingsSmsSwitch.setEnabled(hasNetwork);
        binding.settingsSms.settingsSmsResponseWaitSwitch.setEnabled(hasNetwork);
        binding.settingsSms.settingsSmsReceiver.setEnabled(hasNetwork && smsSettingsViewModel.isGatewayNumberEditable());
        binding.settingsSms.settingsSmsResultSender.setEnabled(hasNetwork && smsSettingsViewModel.isResponseNumberEditable());
        binding.settingsSms.settingsSmsResultTimeout.setEnabled(hasNetwork);

        setUpSmsListeners();
    }

    private void setUpSmsListeners() {
        binding.settingsSms.settingsSmsReceiver.setOnEditorActionListener((view, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && !binding.settingsSms.settingsSmsReceiver.getText().toString().isEmpty()) {
                presenter.saveGatewayNumber(binding.settingsSms.settingsSmsReceiver.getText().toString());
                ExtensionsKt.closeKeyboard(view);
                return true;
            } else {
                return false;
            }
        });

        binding.settingsSms.settingsSmsResultSender.setOnEditorActionListener((view, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && !binding.settingsSms.settingsSmsResultSender.getText().toString().isEmpty()) {
                presenter.saveSmsResultSender(binding.settingsSms.settingsSmsResultSender.getText().toString());
                ExtensionsKt.closeKeyboard(view);
                return true;
            } else {
                return false;
            }
        });

        binding.settingsSms.settingsSmsResultTimeout.setOnEditorActionListener((view, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && !binding.settingsSms.settingsSmsResultTimeout.getText().toString().isEmpty()) {
                presenter.saveSmsResponseTimeout(
                        Integer.valueOf(binding.settingsSms.settingsSmsResultTimeout.getText().toString()));
                ExtensionsKt.closeKeyboard(view);
                return true;
            } else {
                return false;
            }
        });

        binding.settingsSms.settingsSmsResponseWaitSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (!isChecked || !binding.settingsSms.settingsSmsResultSender.getText().toString().isEmpty()) {
                presenter.saveWaitForSmsResponse(isChecked);
            }
        });

        binding.settingsSms.settingsSmsSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (!isChecked || presenter.isGatewaySetAndValid(binding.settingsSms.settingsSmsReceiver.getText().toString())) {
                presenter.enableSmsModule(isChecked);
            }
        });
    }

    @Override
    public void setReservedValuesSettings(ReservedValueSettingsViewModel reservedValueSettingsViewModel) {
        binding.reservedValueLayout.setMessage(
                String.format("%s ", String.valueOf(reservedValueSettingsViewModel.getNumberOfReservedValuesToDownload()))
                        .concat(getString(R.string.settings_reserved_values_message))
        );
        binding.reservedValuesInputLayout.setHint(getString(R.string.settings_reserved_values_message));
        binding.reservedValueEditText.setText(String.valueOf(reservedValueSettingsViewModel.getNumberOfReservedValuesToDownload()));
        binding.reservedValueEditText.setEnabled(reservedValueSettingsViewModel.getCanBeEdited());
        if (reservedValueSettingsViewModel.getCanBeEdited()) {
            binding.reservedValuesInputLayout.setVisibility(View.VISIBLE);
            binding.reservedValueNoEdition.setVisibility(View.GONE);
        } else {
            binding.reservedValuesInputLayout.setVisibility(View.GONE);
            binding.reservedValueNoEdition.setVisibility(View.VISIBLE);
        }

        binding.reservedValueEditText.setOnEditorActionListener((view, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (!binding.reservedValueEditText.getText().toString().isEmpty()) {
                    presenter.saveReservedValues(Integer.valueOf(binding.reservedValueEditText.getText().toString()));
                }
                ExtensionsKt.closeKeyboard(view);
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    public void requestNoEmptySMSGateway() {
        binding.settingsSms.settingsSmsReceiverLayout.setError(
                binding.getRoot().getContext().getResources().getString(R.string.sms_empty_gateway));
    }

    @Override
    public void displaySMSRefreshingData() {
        Snackbar.make(
                binding.getRoot(),
                R.string.sms_downloading_data,
                BaseTransientBottomBar.LENGTH_SHORT).show();
    }

    @Override
    public void displaySMSEnabled(boolean isChecked) {
        Snackbar.make(
                binding.getRoot(),
                isChecked ? R.string.sms_enabled : R.string.sms_disabled,
                BaseTransientBottomBar.LENGTH_SHORT).show();
    }

    private void setNetworkEdition(boolean isOnline) {
        binding.buttonSyncData.setEnabled(isOnline);
        binding.buttonSyncMeta.setEnabled(isOnline);
        binding.buttonSyncData.setAlpha(isOnline ? 1.0f : 0.5f);
        binding.buttonSyncMeta.setAlpha(isOnline ? 1.0f : 0.5f);
    }
}
