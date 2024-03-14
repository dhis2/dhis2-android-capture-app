package org.dhis2.usescases.settings;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE;
import static org.dhis2.bindings.SettingExtensionsKt.EVERY_12_HOUR;
import static org.dhis2.bindings.SettingExtensionsKt.EVERY_24_HOUR;
import static org.dhis2.bindings.SettingExtensionsKt.EVERY_30_MIN;
import static org.dhis2.bindings.SettingExtensionsKt.EVERY_6_HOUR;
import static org.dhis2.bindings.SettingExtensionsKt.EVERY_7_DAYS;
import static org.dhis2.bindings.SettingExtensionsKt.EVERY_HOUR;
import static org.dhis2.commons.Constants.DATA_NOW;
import static org.dhis2.commons.Constants.META_NOW;
import static org.dhis2.commons.Constants.TIME_MANUAL;
import static org.dhis2.commons.extensions.ViewExtensionsKt.closeKeyboard;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.CONFIRM_DELETE_LOCAL_DATA;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.work.WorkInfo;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.dhis2.bindings.ContextExtensionsKt;
import org.dhis2.BuildConfig;
import org.dhis2.Components;
import org.dhis2.R;
import org.dhis2.bindings.ViewExtensionsKt;
import org.dhis2.commons.Constants;
import org.dhis2.commons.animations.ViewAnimationsKt;
import org.dhis2.commons.network.NetworkUtils;
import org.dhis2.commons.resources.ColorType;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.data.service.SyncResult;
import org.dhis2.data.service.workManager.WorkManagerController;
import org.dhis2.databinding.FragmentSettingsBinding;
import org.dhis2.ui.dialogs.alert.AlertDialog;
import org.dhis2.ui.model.ButtonUiModel;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.settings.models.DataSettingsViewModel;
import org.dhis2.usescases.settings.models.ErrorViewModel;
import org.dhis2.usescases.settings.models.MetadataSettingsViewModel;
import org.dhis2.usescases.settings.models.ReservedValueSettingsViewModel;
import org.dhis2.usescases.settings.models.SMSSettingsViewModel;
import org.dhis2.usescases.settings.models.SyncParametersViewModel;
import org.dhis2.usescases.settingsprogram.SettingsProgramActivity;
import org.dhis2.utils.HelpManager;
import org.hisp.dhis.android.core.settings.LimitScope;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import kotlin.Unit;

public class SyncManagerFragment extends FragmentGlobalAbstract implements SyncManagerContracts.View {

    @Inject
    SyncManagerPresenter presenter;

    @Inject
    WorkManagerController workManagerController;

    @Inject
    NetworkUtils networkUtils;

    @Inject
    ColorUtils colorUtils;

    private FragmentSettingsBinding binding;
    private Context context;

    private final BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            presenter.checkData();
            checkSyncDataButtonStatus();
            checkSyncMetaButtonStatus();
        }
    };
    private boolean dataInit;
    private boolean metadataInit;
    private boolean scopeLimitInit;
    private boolean dataWorkRunning;
    private SettingItem settingOpened = null;

    public SyncManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
        ServerComponent serverComponent = ((Components) context.getApplicationContext()).serverComponent();
        if (serverComponent != null) {
            ((Components) context.getApplicationContext()).userComponent()
                    .plus(new SyncManagerModule(this, serverComponent)).inject(this);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false);
        binding.setLifecycleOwner(this);
        binding.setPresenter(presenter);
        binding.smsSettings.setVisibility(ContextExtensionsKt.showSMS(context) ? View.VISIBLE : View.GONE);
        binding.setVersionName(BuildConfig.VERSION_NAME);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        context.registerReceiver(networkReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        workManagerController.getWorkInfosByTagLiveData(META_NOW).observe(this, workStatuses -> {
            WorkInfo.State workState = null;
            if (workStatuses != null && !workStatuses.isEmpty()) {
                workState = workStatuses.get(0).getState();
            }
            presenter.onWorkStatusesUpdate(workState, META_NOW);
            checkSyncMetaButtonStatus();
        });
        workManagerController.getWorkInfosByTagLiveData(DATA_NOW).observe(this, workStatuses -> {
            WorkInfo.State workState = null;
            if (workStatuses != null && !workStatuses.isEmpty()) {
                workState = workStatuses.get(0).getState();
            }
            presenter.onWorkStatusesUpdate(workState, DATA_NOW);
            checkSyncDataButtonStatus();
        });
        presenter.init();
        observeVersionUpdates();

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
    public void deleteLocalData() {
        new AlertDialog(
                getString(R.string.delete_local_data),
                getString(R.string.delete_local_data_message),
                null,
                null,
                R.raw.warning,
                new ButtonUiModel(
                        getString(R.string.cancel),
                        true,
                        () -> null
                ),
                new ButtonUiModel(
                        getString(R.string.action_accept),
                        true,
                        () -> {
                            analyticsHelper().setEvent(CONFIRM_DELETE_LOCAL_DATA, CLICK, CONFIRM_DELETE_LOCAL_DATA);
                            presenter.deleteLocalData();
                            return null;
                        })
        ).show(requireActivity().getSupportFragmentManager());
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
    public void showSyncErrors(List<ErrorViewModel> data) {
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
        if (settingsItem != settingOpened) {
            closedSettingItem(settingOpened);
            settingOpened = settingsItem;
            binding.dataDivider.setVisibility(View.VISIBLE);
            binding.metaDivider.setVisibility(View.VISIBLE);
            binding.parameterDivider.setVisibility(View.VISIBLE);
            binding.reservedValueDivider.setVisibility(View.VISIBLE);

            switch (settingsItem) {
                case DATA_SYNC:
                    ViewAnimationsKt.expand(binding.syncDataActions, true, () -> {
                        binding.syncDataActions.setVisibility(View.VISIBLE);
                        binding.dataDivider.setVisibility(View.GONE);
                        binding.dataSyncBottomShadow.setVisibility(View.VISIBLE);
                        binding.dataSyncTopShadow.setVisibility(View.VISIBLE);
                        scrollToChild(binding.settingsItemData);
                        return Unit.INSTANCE;
                    });
                    break;
                case META_SYNC:
                    ViewAnimationsKt.expand(binding.syncMetadataActions, true, () -> {
                        binding.syncMetadataActions.setVisibility(View.VISIBLE);
                        binding.metaDivider.setVisibility(View.GONE);
                        binding.metaDataTopShadow.setVisibility(View.VISIBLE);
                        binding.metaDataBottomShadow.setVisibility(View.VISIBLE);
                        scrollToChild(binding.settingsItemMeta);
                        return Unit.INSTANCE;
                    });
                    break;
                case SYNC_PARAMETERS:
                    ViewAnimationsKt.expand(binding.parameterData, true, () -> {
                        binding.parameterData.setVisibility(View.VISIBLE);
                        binding.parameterDivider.setVisibility(View.GONE);
                        binding.itemParamsSyncTopShadow.setVisibility(View.VISIBLE);
                        binding.itemParamsSyncBottomShadow.setVisibility(View.VISIBLE);
                        scrollToChild(binding.settingsItemParams);
                        return Unit.INSTANCE;
                    });
                    break;
                case RESERVED_VALUES:
                    ViewAnimationsKt.expand(binding.reservedValuesActions, true, () -> {
                        binding.reservedValuesActions.setVisibility(View.VISIBLE);
                        binding.reservedValueDivider.setVisibility(View.GONE);
                        binding.reservedValueTopShadow.setVisibility(View.VISIBLE);
                        binding.reservedValueBottomShadow.setVisibility(View.VISIBLE);
                        scrollToChild(binding.settingsItemValues);
                        return Unit.INSTANCE;
                    });
                    break;
                case DELETE_LOCAL_DATA:
                    ViewAnimationsKt.expand(binding.deleteDataButton, true, () -> {
                        binding.deleteDataButton.setVisibility(View.VISIBLE);
                        scrollToChild(binding.settingsItemDeleteData);
                        return Unit.INSTANCE;
                    });
                    break;
                case SMS:
                    ViewAnimationsKt.expand(binding.smsContent, true, () -> {
                        binding.smsContent.setVisibility(View.VISIBLE);
                        binding.smsTopShadow.setVisibility(View.VISIBLE);
                        binding.smsBottomShadow.setVisibility(View.VISIBLE);
                        scrollToChild(binding.smsSettings);
                        return Unit.INSTANCE;
                    });
                    break;
                case VERSION_UPDATE:
                    ViewAnimationsKt.expand(binding.versionButton, true, () -> {
                        binding.versionButton.setVisibility(View.VISIBLE);
                        scrollToChild(binding.settingsItemVersion);
                        return Unit.INSTANCE;
                    });
                    break;
                default:
                    break;
            }
        } else {
            closedSettingItem(settingOpened);
            settingOpened = null;
        }
    }

    private void scrollToChild(View child) {
        int[] l = new int[2];
        child.getLocationOnScreen(l);
        Rect rect = new Rect(l[0], l[1], l[0] + child.getWidth(), l[1] + child.getHeight());
        binding.scrollView.requestChildRectangleOnScreen(child,rect, false);
    }

    private void closedSettingItem(SettingItem settingItemToClose) {
        if (settingItemToClose != null) {
            switch (settingItemToClose) {
                case DATA_SYNC:
                    ViewAnimationsKt.collapse(binding.syncDataActions, () -> {
                        binding.syncDataActions.setVisibility(View.GONE);
                        binding.dataSyncTopShadow.setVisibility(View.GONE);
                        binding.dataSyncBottomShadow.setVisibility(View.GONE);
                        return Unit.INSTANCE;
                    });
                    binding.dataDivider.setVisibility(View.VISIBLE);
                    break;
                case META_SYNC:
                    ViewAnimationsKt.collapse(binding.syncMetadataActions, () -> {
                        binding.syncMetadataActions.setVisibility(View.GONE);
                        binding.metaDataTopShadow.setVisibility(View.GONE);
                        binding.metaDataBottomShadow.setVisibility(View.GONE);
                        return Unit.INSTANCE;
                    });
                    binding.metaDivider.setVisibility(View.VISIBLE);
                    break;
                case SYNC_PARAMETERS:
                    ViewAnimationsKt.collapse(binding.parameterData, () -> {
                        binding.parameterData.setVisibility(View.GONE);
                        binding.itemParamsSyncTopShadow.setVisibility(View.GONE);
                        binding.itemParamsSyncBottomShadow.setVisibility(View.GONE);
                        return Unit.INSTANCE;
                    });
                    binding.parameterDivider.setVisibility(View.VISIBLE);
                    break;
                case RESERVED_VALUES:
                    ViewAnimationsKt.collapse(binding.reservedValuesActions, () -> {
                        binding.reservedValuesActions.setVisibility(View.GONE);
                        binding.reservedValueTopShadow.setVisibility(View.GONE);
                        binding.reservedValueBottomShadow.setVisibility(View.GONE);
                        return Unit.INSTANCE;
                    });
                    binding.reservedValueDivider.setVisibility(View.VISIBLE);
                    break;
                case DELETE_LOCAL_DATA:
                    ViewAnimationsKt.collapse(binding.deleteDataButton, () -> {
                        binding.deleteDataButton.setVisibility(View.GONE);
                        return Unit.INSTANCE;
                    });
                    break;
                case SMS:
                    ViewAnimationsKt.collapse(binding.smsContent, () -> {
                        binding.smsContent.setVisibility(View.GONE);
                        binding.smsTopShadow.setVisibility(View.GONE);
                        binding.smsBottomShadow.setVisibility(View.GONE);
                        return Unit.INSTANCE;
                    });
                    break;
                case VERSION_UPDATE:
                    ViewAnimationsKt.collapse(binding.versionButton, () -> {
                        binding.versionButton.setVisibility(View.GONE);
                        return Unit.INSTANCE;
                    });
                    break;
                default:
                    break;
            }
        }
    }

    private String dataSyncSetting() {
        int timeData = presenter.getDataPeriodSetting();
        String setting;
        switch (timeData) {
            case EVERY_30_MIN:
                setting = getString(R.string.thirty_minutes);
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
            String errorMessage;
            int textColor;
            if (dataSettings.getSyncResult() != null &&
                    dataSettings.getSyncResult().equals(SyncResult.INCOMPLETE)) {
                errorMessage = getString(R.string.sync_incomplete_error_text);
                textColor = ContextCompat.getColor(context, R.color.text_black_4d4d4d);
            } else {
                errorMessage = getString(R.string.sync_error_text);
                textColor = ContextCompat.getColor(context, R.color.red_060);
            }
            String dataText = dataSyncSetting()
                    .concat("\n")
                    .concat(errorMessage);
            binding.syncDataLayout.message.setText(dataText);
            binding.syncDataLayout.message.setTextColor(textColor);
        }

        if (dataSettings.getDataHasErrors()) {
            String src = dataSyncSetting()
                    .concat("\n")
                    .concat(getString(R.string.data_sync_error));
            SpannableString str = new SpannableString(src);
            int wIndex = src.indexOf('@');
            int eIndex = src.indexOf('$');
            str.setSpan(new ImageSpan(context, R.drawable.ic_sync_warning), wIndex, wIndex + 1, SPAN_EXCLUSIVE_EXCLUSIVE);
            str.setSpan(new ImageSpan(context, R.drawable.ic_sync_problem_red), eIndex, eIndex + 1, SPAN_EXCLUSIVE_EXCLUSIVE);
            binding.syncDataLayout.message.setText(str);
            binding.syncDataLayout.message.setTextColor(ContextCompat.getColor(context, R.color.red_060));

        } else if (dataSettings.getDataHasWarnings()) {
            String src = dataSyncSetting().concat("\n").concat(getString(R.string.data_sync_warning));
            SpannableString str = new SpannableString(src);
            int wIndex = src.indexOf('@');
            str.setSpan(new ImageSpan(context, R.drawable.ic_sync_warning), wIndex, wIndex + 1, SPAN_EXCLUSIVE_EXCLUSIVE);
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
            case EVERY_30_MIN:
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
                            saveTimeData(EVERY_30_MIN);
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
            binding.metadataPeriodsHint.setVisibility(View.VISIBLE);
            binding.metaPeriodsNoEdition.setVisibility(View.GONE);
        } else {
            binding.metadataPeriods.setVisibility(View.GONE);
            binding.metadataPeriodsHint.setVisibility(View.GONE);
            binding.metaPeriodsNoEdition.setVisibility(View.VISIBLE);
        }

        binding.metadataPeriods.setEnabled(metadataSettings.getCanEdit());
        binding.metadataPeriods.setAdapter(new ArrayAdapter<>(context, R.layout.spinner_settings_item,
                context.getResources().getStringArray(R.array.metadata_sync_periods)));
        switch (metadataSettings.getMetadataSyncPeriod()) {
            case EVERY_7_DAYS:
                binding.metadataPeriods.setSelection(1);
                break;
            case TIME_MANUAL:
                binding.metadataPeriods.setSelection(2);
                break;
            case EVERY_24_HOUR:
            default:
                binding.metadataPeriods.setSelection(0);
                break;
        }
        binding.metadataPeriods.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (metadataInit) {
                    switch (position) {
                        case 0:
                            saveTimeMeta(EVERY_24_HOUR);
                            break;
                        case 1:
                            saveTimeMeta(EVERY_7_DAYS);
                            break;
                        case 2:
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
            str.setSpan(new ForegroundColorSpan(colorUtils.getPrimaryColor(context, ColorType.PRIMARY)),
                    indexOfNumber,
                    indexOfNumber + 1,
                    SPAN_INCLUSIVE_EXCLUSIVE);
            binding.specificSettingsText.setText(str);
            binding.specificSettingsButton.setOnClickListener(view ->
                    startActivity(SettingsProgramActivity.Companion.getIntentActivity(context)));
        } else {
            binding.specificSettingsText.setVisibility(View.GONE);
            binding.specificSettingsButton.setVisibility(View.GONE);
        }

        if (parameterSettings.getLimitScopeIsEditable()) {
            binding.downloadLimitScopeHint.setVisibility(View.VISIBLE);
            binding.downloadLimitScope.setVisibility(View.VISIBLE);
            binding.eventsInputLayout.setVisibility(View.VISIBLE);
            binding.teiInputLayout.setVisibility(View.VISIBLE);
            binding.parametersNoEdition.setVisibility(View.GONE);
            setUpSyncParameterListeners();
        } else {
            binding.downloadLimitScopeHint.setVisibility(View.GONE);
            binding.downloadLimitScope.setVisibility(View.GONE);
            binding.eventsInputLayout.setVisibility(View.GONE);
            binding.teiInputLayout.setVisibility(View.GONE);
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
        if (binding.downloadLimitScope.getOnItemSelectedListener() == null) {
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
        }

        binding.eventsEditText.setOnEditorActionListener((view, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                closeKeyboard(view);
                if (!binding.eventsEditText.getText().toString().isEmpty()) {
                    presenter.saveEventMaxCount(Integer.valueOf(binding.eventsEditText.getText().toString()));
                }
                return true;
            } else {
                return false;
            }
        });

        binding.teiEditText.setOnEditorActionListener((view, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (!binding.teiEditText.getText().toString().isEmpty()) {
                    closeKeyboard(view);
                    presenter.saveTeiMaxCount(Integer.valueOf(binding.teiEditText.getText().toString()));
                }
                return true;
            } else {
                return false;
            }
        });

        binding.eventsEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !binding.eventsEditText.getText().toString().isEmpty()) {
                presenter.saveEventMaxCount(Integer.valueOf(binding.eventsEditText.getText().toString()));
            }
        });

        binding.teiEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !binding.teiEditText.getText().toString().isEmpty()) {
                presenter.saveTeiMaxCount(Integer.valueOf(binding.teiEditText.getText().toString()));
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

        boolean smsSwitchEnabled = false;
        boolean smsResponseEnabled = false;

        if (!binding.settingsSms.settingsSmsReceiver.getText().toString().isEmpty()) {
            presenter.validateGatewayObservable(binding.settingsSms.settingsSmsReceiver.getText().toString());
            smsSwitchEnabled = true;
        }

        if (!binding.settingsSms.settingsSmsResultSender.getText().toString().isEmpty()) {
            smsResponseEnabled = true;
        }

        boolean hasNetwork = networkUtils.isOnline();

        binding.settingsSms.settingsSmsReceiver.setEnabled(hasNetwork && smsSettingsViewModel.isGatewayNumberEditable());
        binding.settingsSms.settingsSmsResultTimeout.setEnabled(hasNetwork);
        binding.settingsSms.settingsSmsSwitch.setEnabled(smsSwitchEnabled);

        binding.settingsSms.settingsSmsResponseWaitSwitch.setEnabled(smsResponseEnabled);
        binding.settingsSms.settingsSmsResultSender.setEnabled(smsResponseEnabled);

        setUpSmsListeners();
    }

    private void setUpSmsListeners() {
        ViewExtensionsKt.clearFocusOnDone(binding.settingsSms.settingsSmsReceiver);
        ViewExtensionsKt.clearFocusOnDone(binding.settingsSms.settingsSmsResultSender);
        ViewExtensionsKt.clearFocusOnDone(binding.settingsSms.settingsSmsResultTimeout);

        binding.settingsSms.settingsSmsReceiver.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                presenter.checkGatewayAndTimeoutAreValid();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ViewExtensionsKt.onFocusRemoved(binding.settingsSms.settingsSmsReceiver, () -> {
            presenter.saveGatewayNumber(binding.settingsSms.settingsSmsReceiver.getText().toString());
            presenter.checkGatewayAndTimeoutAreValid();
            return Unit.INSTANCE;
        });

        binding.settingsSms.settingsSmsResultSender.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (text != null && !text.toString().isEmpty()) {
                    enabledResponseWaitSwitch();
                } else {
                    binding.settingsSms.settingsSmsResponseWaitSwitch.setChecked(false);
                    binding.settingsSms.settingsSmsResponseWaitSwitch.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ViewExtensionsKt.onFocusRemoved(binding.settingsSms.settingsSmsResultSender, () -> {
            presenter.saveSmsResultSender(binding.settingsSms.settingsSmsResultSender.getText().toString());
            enabledResponseWaitSwitch();
            return Unit.INSTANCE;
        });

        ViewExtensionsKt.onFocusRemoved(binding.settingsSms.settingsSmsResultTimeout, () -> {
            if (!binding.settingsSms.settingsSmsResultTimeout.getText().toString().isEmpty()) {
                presenter.saveSmsResponseTimeout(Integer.valueOf(binding.settingsSms.settingsSmsResultTimeout.getText().toString()));
                presenter.checkGatewayAndTimeoutAreValid();
            }
            return Unit.INSTANCE;
        });

        binding.settingsSms.settingsSmsResponseWaitSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            clearSmsFocus();
            if (!isChecked || !binding.settingsSms.settingsSmsResultSender.getText().toString().isEmpty()) {
                presenter.saveWaitForSmsResponse(isChecked);
            }
        });

        binding.settingsSms.settingsSmsSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            clearSmsFocus();
            if (binding.settingsSms.settingsSmsReceiverLayout.getError() != null) {
                binding.settingsSms.settingsSmsSwitch.setChecked(false);
                requestNoEmptySMSGateway();
            }
            if (!isChecked || presenter.isGatewaySetAndValid(binding.settingsSms.settingsSmsReceiver.getText().toString())) {
                presenter.enableSmsModule(isChecked);
            }
        });
    }

    private void clearSmsFocus() {
        binding.settingsSms.settingsSmsReceiver.clearFocus();
        binding.settingsSms.settingsSmsResultSender.clearFocus();
        binding.settingsSms.settingsSmsResultTimeout.clearFocus();
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
            closeKeyboard(view);
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (!binding.reservedValueEditText.getText().toString().isEmpty()) {
                    presenter.saveReservedValues(Integer.valueOf(binding.reservedValueEditText.getText().toString()));
                }
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

    @Override
    public void displaySmsEnableError() {
        binding.settingsSms.settingsSmsSwitch.setChecked(false);
    }

    @Override
    public void onMetadataSyncInProgress() {
        binding.syncMetaLayout.message.setTextColor(ContextCompat.getColor(context, R.color.text_black_333));
        String metaText = metaSyncSettings().concat("\n").concat(context.getString(R.string.syncing_configuration));
        binding.syncMetaLayout.message.setText(metaText);
    }

    @Override
    public void onMetadataFinished() {
        presenter.checkData();
    }

    @Override
    public void onDataSyncInProgress() {
        String dataText = dataSyncSetting().concat("\n").concat(context.getString(R.string.syncing_data));
        binding.syncDataLayout.message.setTextColor(ContextCompat.getColor(context, R.color.text_black_333));
        binding.syncDataLayout.message.setText(dataText);
        dataWorkRunning = true;
    }

    @Override
    public void onDataFinished() {
        dataWorkRunning = false;
        presenter.checkData();
    }

    private void checkSyncDataButtonStatus() {
        boolean isOnline = networkUtils.isOnline();
        boolean canBeClicked = isOnline && !dataWorkRunning;
        presenter.updateSyncDataButton(canBeClicked);
    }

    private void checkSyncMetaButtonStatus() {
        boolean isOnline = networkUtils.isOnline();
        boolean canBeClicked = isOnline && !metadataInit;
        presenter.updateSyncMetaDataButton(canBeClicked);
    }

    @Override
    public boolean isGatewayValid() {
        return binding.settingsSms.settingsSmsReceiverLayout.getError() == null &&
                !binding.settingsSms.settingsSmsReceiver.getText().toString().isEmpty();
    }

    @Override
    public boolean isResultTimeoutValid() {
        return binding.settingsSms.settingsSmsResultTimeoutLayout.getError() == null &&
                !binding.settingsSms.settingsSmsResultTimeout.getText().toString().isEmpty();
    }

    @Override
    public void enabledSMSSwitchAndSender(SMSSettingsViewModel settingsViewModel) {
        binding.settingsSms.settingsSmsSwitch.setEnabled(networkUtils.isOnline());
        binding.settingsSms.settingsSmsResultSender.setEnabled(networkUtils.isOnline() && settingsViewModel.isResponseNumberEditable());
    }

    private void enabledResponseWaitSwitch() {
        binding.settingsSms.settingsSmsResponseWaitSwitch.setEnabled(networkUtils.isOnline());
    }

    private void observeVersionUpdates() {
        presenter.getUpdatesLoading().observe(getViewLifecycleOwner(), loading -> {
            if (Boolean.TRUE.equals(loading)) {
                ViewAnimationsKt.expand(binding.loadingCheckVersion, true, () -> Unit.INSTANCE);
            }
        });
        presenter.getVersionToUpdate().observe(getViewLifecycleOwner(), newVersion -> {
            binding.loadingCheckVersion.setVisibility(View.INVISIBLE);
            if (newVersion == null) {
                Snackbar.make(
                        binding.getRoot(),
                        R.string.no_updates,
                        BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        });
    }
}
