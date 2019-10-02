package org.dhis2.usescases.main.program;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.dhis2.App;
import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.service.SyncGranularRxWorker;
import org.dhis2.databinding.SyncBottomDialogBinding;
import org.dhis2.usescases.sms.SmsSendingService;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.NetworkUtils;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.custom_views.MessageAmountDialog;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataset.DataSetElement;
import org.hisp.dhis.android.core.datavalue.DataValue;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.hisp.dhis.android.core.program.ProgramType;
import org.hisp.dhis.android.core.sms.domain.interactor.SmsSubmitCase;
import org.hisp.dhis.android.core.sms.domain.repository.SmsRepository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static org.dhis2.utils.Constants.ATTRIBUTE_OPTION_COMBO;
import static org.dhis2.utils.Constants.CONFLICT_TYPE;
import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PERIOD_ID;
import static org.dhis2.utils.Constants.UID;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SYNC_GRANULAR;
import static org.dhis2.utils.analytics.AnalyticsConstants.SYNC_GRANULAR_ONLINE;
import static org.dhis2.utils.analytics.AnalyticsConstants.SYNC_GRANULAR_SMS;

@SuppressLint("ValidFragment")
public class SyncStatusDialog extends BottomSheetDialogFragment {

    private String recordUid;
    private final CompositeDisposable compositeDisposable;
    private final ConflictType conflictType;
    private D2 d2;
    private SyncBottomDialogBinding binding;
    private SyncConflictAdapter adapter;
    private String orgUnitDataValue;
    private String attributeComboDataValue;
    private String periodIdDataValue;
    private FlowableProcessor processor;

    public AnalyticsHelper analyticsHelper;
    private Context context;
    private MutableLiveData<List<SmsSendingService.SendingStatus>> states;
    private SmsSubmitCase smsSender;
    private ArrayList<SmsSendingService.SendingStatus> statesList;

    public enum ConflictType {
        PROGRAM, TEI, EVENT, DATA_SET, DATA_VALUES
    }

    @SuppressLint("ValidFragment")
    public SyncStatusDialog(String recordUid, ConflictType conflictType, FlowableProcessor processor, AnalyticsHelper analyticsHelper) {
        this.recordUid = recordUid;
        this.conflictType = conflictType;
        this.compositeDisposable = new CompositeDisposable();
        this.processor = processor;
        this.analyticsHelper = analyticsHelper;
    }

    @SuppressLint("ValidFragment")
    public SyncStatusDialog(String orgUnitDataValue, String attributeComboDataValue, String periodIdDataValue,
                            ConflictType conflictType, FlowableProcessor processor, AnalyticsHelper analyticsHelper) {
        this.orgUnitDataValue = orgUnitDataValue;
        this.attributeComboDataValue = attributeComboDataValue;
        this.periodIdDataValue = periodIdDataValue;
        this.conflictType = conflictType;
        this.compositeDisposable = new CompositeDisposable();
        this.processor = processor;
        this.analyticsHelper = analyticsHelper;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        d2 = ((App) context.getApplicationContext()).serverComponent().userManager().getD2();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.sync_bottom_dialog, container, false);
        adapter = new SyncConflictAdapter(new ArrayList<>());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.synsStatusRecycler.setLayoutManager(layoutManager);
        binding.synsStatusRecycler.setAdapter(adapter);

        switch (conflictType) {
            case PROGRAM:
                configureForProgram();
                break;
            case TEI:
                configureForTei();
                break;
            case EVENT:
                configureForEvent();
                break;
            case DATA_SET:
                configureForDataSet();
                break;
            case DATA_VALUES:
                configureForDataValue();
        }
        setRetainInstance(true);

        return binding.getRoot();
    }

    private void configureForDataValue() {
        binding.programName.setText(R.string.qr_data_values);

        compositeDisposable.add(
                Observable.fromCallable(() -> d2.dataValueModule().dataValues.byOrganisationUnitUid().eq(orgUnitDataValue)
                        .byAttributeOptionComboUid().eq(attributeComboDataValue)
                        .byPeriod().eq(periodIdDataValue).blockingGet())
                        .map(dataSetElements -> {
                            State state = State.SYNCED;
                            for (DataValue dataValue : dataSetElements) {
                                if (dataValue.state() != State.SYNCED)
                                    state = State.TO_UPDATE;
                            }

                            return state;
                        }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(
                        state -> {
                            Bindings.setStateIcon(binding.syncIcon, state);
                            binding.syncStatusName.setText(getTextByState(state));
                            binding.syncStatusBar.setBackgroundResource(getColorForState(state));
                            switch (state) {
                                case TO_POST:
                                case TO_UPDATE:
                                    setNoConflictMessage(getString(R.string.no_conflicts_update_message));
                                    break;
                                case SYNCED:
                                    setNoConflictMessage(getString(R.string.no_conflicts_synced_message));
                                    break;
                                case WARNING:
                                case ERROR:
                                    setProgramConflictMessage(state);
                                    break;
                                default:
                                    break;
                            }
                        },
                        error -> dismiss()
                ));
    }

    private void configureForDataSet() {
        compositeDisposable.add(
                Observable.fromCallable(() -> d2.dataSetModule().dataSets.uid(recordUid).blockingGet())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                dataset -> binding.programName.setText(dataset.displayName()),
                                error -> dismiss()
                        )
        );
        compositeDisposable.add(
                Observable.fromCallable(() -> d2.dataSetModule().dataSets.withDataSetElements().uid(recordUid).blockingGet().dataSetElements())
                        .map(dataSetElements -> {
                            State state = State.SYNCED;
                            for (DataSetElement dataSetElement : dataSetElements) {
                                for (DataValue dataValue : d2.dataValueModule().dataValues.byDataElementUid().eq(dataSetElement.dataElement().uid()).blockingGet()) {
                                    if (dataValue.state() != State.SYNCED)
                                        state = State.TO_UPDATE;
                                }
                            }

                            return state;
                        }).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(
                        state -> {
                            Bindings.setStateIcon(binding.syncIcon, state);
                            binding.syncStatusName.setText(getTextByState(state));
                            binding.syncStatusBar.setBackgroundResource(getColorForState(state));
                            switch (state) {
                                case TO_POST:
                                case TO_UPDATE:
                                    setNoConflictMessage(getString(R.string.no_conflicts_update_message));
                                    break;
                                case SYNCED:
                                    setNoConflictMessage(getString(R.string.no_conflicts_synced_message));
                                    break;
                                case WARNING:
                                case ERROR:
                                    setProgramConflictMessage(state);
                                    break;
                                default:
                                    break;
                            }
                        },
                        error -> dismiss()
                ));

    }


    private void configureForProgram() {

        compositeDisposable.add(
                Observable.fromCallable(() -> d2.programModule().programs.uid(recordUid).blockingGet())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                program -> binding.programName.setText(program.displayName()),
                                error -> dismiss()
                        )
        );

        compositeDisposable.add(
                Observable.fromCallable(() -> d2.programModule().programs.uid(recordUid).blockingGet())
                        .map(program -> {
                            State state = State.SYNCED;
                            if (program.programType() == ProgramType.WITHOUT_REGISTRATION) {
                                if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.ERROR).blockingGet().isEmpty())
                                    state = State.ERROR;
                                else if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.WARNING).blockingGet().isEmpty())
                                    state = State.WARNING;
                                else if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).blockingGet().isEmpty())
                                    state = State.SENT_VIA_SMS;
                                else if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.TO_UPDATE, State.TO_POST).blockingGet().isEmpty() ||
                                        !d2.eventModule().events.byProgramUid().eq(program.uid()).byDeleted().isTrue().blockingGet().isEmpty())
                                    state = State.TO_UPDATE;
                            } else {
                                List<String> programUids = new ArrayList<>();
                                programUids.add(program.uid());
                                if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.ERROR).blockingGet().isEmpty())
                                    state = State.ERROR;
                                else if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.WARNING).blockingGet().isEmpty())
                                    state = State.WARNING;
                                else if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).blockingGet().isEmpty())
                                    state = State.SENT_VIA_SMS;
                                else if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.TO_UPDATE, State.TO_POST).blockingGet().isEmpty() ||
                                        !d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byDeleted().isTrue().blockingGet().isEmpty())
                                    state = State.TO_UPDATE;
                            }
                            return state;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                state -> {
                                    switch (state) {
                                        case TO_POST:
                                        case TO_UPDATE:
                                            setNoConflictMessage(getString(R.string.no_conflicts_update_message));
                                            break;
                                        case SYNCED:
                                            setNoConflictMessage(getString(R.string.no_conflicts_synced_message));
                                            break;
                                        case WARNING:
                                        case ERROR:
                                            setProgramConflictMessage(state);
                                            break;
                                        default:
                                            break;
                                    }
                                },
                                error -> dismiss()
                        )
        );

        compositeDisposable.add(
                Observable.fromCallable(() -> d2.programModule().programs.uid(recordUid).blockingGet())
                        .map(program -> {
                            State state = State.SYNCED;
                            if (program.programType() == ProgramType.WITH_REGISTRATION) {
                                List<String> programUids = new ArrayList<>();
                                programUids.add(program.uid());
                                if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.ERROR, State.WARNING).blockingGet().isEmpty())
                                    state = State.WARNING;
                                else if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).blockingGet().isEmpty())
                                    state = State.SENT_VIA_SMS;
                                else if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.TO_UPDATE, State.TO_POST).blockingGet().isEmpty() ||
                                        !d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byDeleted().isTrue().blockingGet().isEmpty())
                                    state = State.TO_UPDATE;
                            } else {
                                if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.ERROR, State.WARNING).blockingGet().isEmpty())
                                    state = State.WARNING;
                                else if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).blockingGet().isEmpty())
                                    state = State.SENT_VIA_SMS;
                                else if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.TO_UPDATE, State.TO_POST).blockingGet().isEmpty() ||
                                        !d2.eventModule().events.byProgramUid().eq(program.uid()).byDeleted().isTrue().blockingGet().isEmpty())
                                    state = State.TO_UPDATE;
                            }
                            return state;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                state -> {
                                    Bindings.setStateIcon(binding.syncIcon, state);
                                    binding.syncStatusName.setText(getTextByState(state));
                                    binding.syncStatusBar.setBackgroundResource(getColorForState(state));
                                },
                                error -> dismiss()
                        )
        );

    }


    private void configureForTei() {

        compositeDisposable.add(
                Observable.fromCallable(() -> d2.trackedEntityModule().trackedEntityTypes
                        .uid(d2.trackedEntityModule().trackedEntityInstances.byUid().eq(recordUid).one().blockingGet().trackedEntityType())
                        .blockingGet())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                trackedEntityType -> binding.programName.setText(trackedEntityType.displayName()),
                                error -> dismiss()
                        )
        );

        compositeDisposable.add(
                Observable.fromCallable(() -> d2.importModule().trackerImportConflicts.byTrackedEntityInstanceUid().eq(recordUid).blockingGet())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                conflicts -> {
                                    if (!conflicts.isEmpty())
                                        prepareConflictAdapter(conflicts);
                                },
                                error -> dismiss()
                        )
        );

        compositeDisposable.add(
                Observable.fromCallable(() -> d2.trackedEntityModule().trackedEntityInstances.byUid().eq(recordUid).one().blockingGet().state())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                state -> {
                                    Bindings.setStateIcon(binding.syncIcon, state);
                                    binding.syncStatusName.setText(getTextByState(state));
                                    binding.syncStatusBar.setBackgroundResource(getColorForState(state));
                                    switch (state) {
                                        case TO_POST:
                                        case TO_UPDATE:
                                            setNoConflictMessage(getString(R.string.no_conflicts_update_message));
                                            break;
                                        case SYNCED:
                                            setNoConflictMessage(getString(R.string.no_conflicts_synced_message));
                                            break;
                                    }
                                },
                                error -> dismiss()
                        )
        );
    }

    private void configureForEvent() {
        binding.programName.setText(R.string.event_event);

        compositeDisposable.add(
                Observable.fromCallable(() -> d2.importModule().trackerImportConflicts.byEventUid().eq(recordUid).blockingGet())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                conflicts -> {
                                    if (!conflicts.isEmpty())
                                        prepareConflictAdapter(conflicts);
                                },
                                error -> dismiss()
                        )
        );

        compositeDisposable.add(
                Observable.fromCallable(() -> d2.eventModule().events.uid(recordUid).blockingGet().state())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                state -> {
                                    Bindings.setStateIcon(binding.syncIcon, state);
                                    binding.syncStatusName.setText(getTextByState(state));
                                    binding.syncStatusBar.setBackgroundResource(getColorForState(state));
                                    switch (state) {
                                        case TO_POST:
                                        case TO_UPDATE:
                                            setNoConflictMessage(getString(R.string.no_conflicts_update_message));
                                            break;
                                        case SYNCED:
                                            setNoConflictMessage(getString(R.string.no_conflicts_synced_message));
                                            break;
                                    }
                                },
                                error -> dismiss()
                        )
        );
    }

    private void setNetworkMessage() {
        if (!NetworkUtils.isOnline(getContext())) {
            if (d2.smsModule().configCase().getSmsModuleConfig().blockingGet().isModuleEnabled()) {
                analyticsHelper.setEvent(SYNC_GRANULAR_SMS, CLICK, SYNC_GRANULAR);
                binding.connectionMessage.setText(R.string.network_unavailable_sms);
                binding.syncButton.setText(R.string.action_sync_sms);
                binding.syncButton.setVisibility(View.VISIBLE);
                binding.syncButton.setOnClickListener(view -> syncSMS());

            } else {
                analyticsHelper.setEvent(SYNC_GRANULAR_ONLINE, CLICK, SYNC_GRANULAR);
                binding.connectionMessage.setText(R.string.network_unavailable);
                binding.syncButton.setVisibility(View.INVISIBLE);
                binding.syncButton.setOnClickListener(null);
            }
        } else {
            binding.connectionMessage.setText(null);
            binding.syncButton.setText(R.string.action_sync);
            if (binding.syncStatusName.getText().equals(getString(R.string.state_synced)))
                binding.syncButton.setVisibility(View.GONE);

            binding.syncButton.setOnClickListener(view -> syncGranular());
        }
    }

    private void prepareConflictAdapter(List<TrackerImportConflict> conflicts) {
        binding.synsStatusRecycler.setVisibility(View.VISIBLE);
        binding.noConflictMessage.setVisibility(View.GONE);

        List<StatusLogItem> listStatusLog = new ArrayList<>();

        for (TrackerImportConflict tracker : conflicts)
            listStatusLog.add(StatusLogItem.create(tracker.created(), tracker.conflict()));

        adapter.addItems(listStatusLog);
        setNetworkMessage();
    }

    private void setNoConflictMessage(String message) {
        binding.synsStatusRecycler.setVisibility(View.GONE);
        binding.noConflictMessage.setText(message);
        binding.noConflictMessage.setVisibility(View.VISIBLE);
        setNetworkMessage();

    }


    private void setProgramConflictMessage(State state) {
        binding.synsStatusRecycler.setVisibility(View.GONE);
        binding.noConflictMessage.setVisibility(View.VISIBLE);

        String src = getString(state == State.WARNING ? R.string.data_sync_warning_program : R.string.data_sync_error_program);
        SpannableString str = new SpannableString(src);
        int wIndex = src.indexOf('@');
        int eIndex = src.indexOf('$');
        if (wIndex > -1)
            str.setSpan(new ImageSpan(getContext(), R.drawable.ic_sync_warning), wIndex, wIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (eIndex > -1)
            str.setSpan(new ImageSpan(getContext(), R.drawable.ic_sync_problem_red), eIndex, eIndex + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.noConflictMessage.setText(str);
        setNetworkMessage();
    }

    private int getTextByState(State state) {

        switch (state) {
            case SYNCED_VIA_SMS:
            case SENT_VIA_SMS:
                return R.string.sync_by_sms;
            case WARNING:
                return R.string.state_warning;
            case ERROR:
                return R.string.state_error;
            case TO_UPDATE:
                return R.string.state_to_update;
            case TO_POST:
                return R.string.state_to_post;
            default:
                return R.string.state_synced;

        }
    }

    private int getColorForState(State state) {

        switch (state) {
            case SYNCED_VIA_SMS:
            case SENT_VIA_SMS:
                return R.color.state_by_sms;
            case WARNING:
                return R.color.state_warning;
            case ERROR:
                return R.color.state_error;
            case TO_UPDATE:
            case TO_POST:
                return R.color.state_to_post;
            default:
                return R.color.state_synced;

        }
    }

    //This is necessary to show the bottomSheet dialog with full height on landscape
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            BottomSheetDialog dialog = (BottomSheetDialog) getDialog();

            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setPeekHeight(0);
        });
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        compositeDisposable.clear();
        processor.onNext(true);
        super.onDismiss(dialog);
    }

    private void syncSMS() {
        binding.noConflictMessage.setVisibility(View.GONE);
        binding.synsStatusRecycler.setVisibility(View.VISIBLE);
        smsSender = d2.smsModule().smsSubmitCase();
        statesList = new ArrayList<>();
        states = new MutableLiveData<>();
        states.observe(getActivity(), states1 -> stateChanged(states1));

        reportState(SmsSendingService.State.STARTED, 0, 0);
        Single<Integer> convertTask;
        switch (conflictType) {
            case EVENT:
                convertTask = smsSender.convertSimpleEvent(recordUid);
                break;
            case TEI:
                //TODO: GET ALL ENROLLMENTS FROM TEI
                List<String> enrollmentUids = UidsHelper.getUidsList(d2.enrollmentModule().enrollments.byTrackedEntityInstance().eq(recordUid)
                        .byState().in(State.TO_POST, State.TO_UPDATE).blockingGet());
                convertTask = smsSender.convertEnrollment(enrollmentUids.get(0));
                break;
            case DATA_VALUES:
                convertTask = smsSender.convertDataSet(recordUid, orgUnitDataValue, periodIdDataValue, attributeComboDataValue);
                break;
            default:
                return;
        }


        compositeDisposable.add(
                convertTask
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(
                                count -> {
                                    reportState(SmsSendingService.State.CONVERTED, 0, count);
                                    reportState(SmsSendingService.State.WAITING_COUNT_CONFIRMATION, 0, count);
                                },
                                this::reportError
                        )
        );
    }

    private void stateChanged(List<SmsSendingService.SendingStatus> states) {
        if (states == null) return;

        for (SmsSendingService.SendingStatus status : states) {
            if (status.state.isSending()) {
                adapter.addItem(StatusLogItem.create(new Date(), getString(R.string.sms_bar_state_sending)));
                adapter.addItem(StatusLogItem.create(new Date(), status.sent + "/" + status.total));
            } else if (status.state.isError())
                adapter.addItem(StatusLogItem.create(new Date(), getString(R.string.sms_bar_state_failed)));
            else if (status.state.isCompleted())
                adapter.addItem(StatusLogItem.create(new Date(), getString(R.string.sms_bar_state_sent)));
        }
        if (states.size() == 0) {
            return;
        }
        SmsSendingService.SendingStatus lastState = states.get(states.size() - 1);
        switch (lastState.state) {
            case WAITING_COUNT_CONFIRMATION:
                askForMessagesAmount(lastState.total);
            case STARTED:
            case CONVERTED:
            case SENDING:
            case WAITING_RESULT:
            case RESULT_CONFIRMED:
            case SENT:
                break;
            case ITEM_NOT_READY:
            case COUNT_NOT_ACCEPTED:
            case WAITING_RESULT_TIMEOUT:
            case ERROR:
            case COMPLETED:
                break;
        }
    }

    private void askForMessagesAmount(int amount) {
        Bundle args = new Bundle();
        args.putInt(MessageAmountDialog.ARG_AMOUNT, amount);
        MessageAmountDialog dialog = new MessageAmountDialog(accepted -> {
            if (accepted)
                sendSMS();
            else
                reportState(SmsSendingService.State.COUNT_NOT_ACCEPTED, 0, 0);
        });
        dialog.setArguments(args);
        dialog.show(getActivity().getSupportFragmentManager(), null);
    }

    private void reportState(SmsSendingService.State state, int sent, int total) {
        Integer submissionId = (smsSender != null) ? smsSender.getSubmissionId() : null;
        SmsSendingService.SendingStatus currentStatus = new SmsSendingService.SendingStatus(submissionId, state, null, sent, total);
        statesList.add(currentStatus);
        states.postValue(statesList);
    }

    private void reportError(Throwable throwable) {
        Timber.tag(SmsSendingService.class.getSimpleName()).e(throwable);
        Integer submissionId = (smsSender != null) ? smsSender.getSubmissionId() : null;
        statesList.add(new SmsSendingService.SendingStatus(submissionId, SmsSendingService.State.ERROR, throwable, 0, 0));
        states.postValue(statesList);
    }

    private void sendSMS() {
        Date startDate = new Date();
        compositeDisposable.add(smsSender.send().doOnNext(state -> {
            if (!isLastSendingStateTheSame(state.getSent(), state.getTotal())) {
                reportState(SmsSendingService.State.SENDING, state.getSent(), state.getTotal());
            }
        }).ignoreElements().doOnComplete(() -> reportState(SmsSendingService.State.SENT, 0, 0)).andThen(
                d2.smsModule().configCase().getSmsModuleConfig()
        ).flatMapCompletable(config -> {
            if (config.isWaitingForResult()) {
                reportState(SmsSendingService.State.WAITING_RESULT, 0, 0);
                return smsSender.checkConfirmationSms(startDate)
                        .doOnError(throwable -> {
                            if (throwable instanceof SmsRepository.ResultResponseException) {
                                SmsRepository.ResultResponseIssue reason =
                                        ((SmsRepository.ResultResponseException) throwable).getReason();
                                if (reason == SmsRepository.ResultResponseIssue.TIMEOUT) {
                                    reportState(SmsSendingService.State.WAITING_RESULT_TIMEOUT, 0, 0);
                                }
                            }
                        }).doOnComplete(() ->
                                reportState(SmsSendingService.State.RESULT_CONFIRMED, 0, 0));
            } else {
                return Completable.complete();
            }
        }).subscribeOn(Schedulers.newThread()
        ).observeOn(Schedulers.newThread()
        ).subscribeWith(new DisposableCompletableObserver() {
            @Override
            public void onError(Throwable e) {
                reportError(e);
            }

            @Override
            public void onComplete() {
                reportState(SmsSendingService.State.COMPLETED, 0, 0);
            }
        }));
    }

    private boolean isLastSendingStateTheSame(int sent, int total) {
        if (statesList == null || statesList.size() == 0) return false;
        SmsSendingService.SendingStatus last = statesList.get(statesList.size() - 1);
        return last.state == SmsSendingService.State.SENDING && last.sent == sent && last.total == total;
    }

    private void syncGranular() {
        OneTimeWorkRequest.Builder syncGranularEventBuilder = new OneTimeWorkRequest.Builder(SyncGranularRxWorker.class);
        syncGranularEventBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());

        ConflictType conflictTypeData = null;
        Data dataToDataValues = null;
        switch (conflictType) {
            case PROGRAM:
                conflictTypeData = ConflictType.PROGRAM;
                break;
            case TEI:
                conflictTypeData = ConflictType.TEI;
                break;
            case EVENT:
                conflictTypeData = ConflictType.EVENT;
                break;
            case DATA_SET:
                conflictTypeData = ConflictType.DATA_SET;
                break;
            case DATA_VALUES:
                dataToDataValues = new Data.Builder().putString(UID, recordUid)
                        .putString(CONFLICT_TYPE, ConflictType.DATA_VALUES.name())
                        .putString(ORG_UNIT, orgUnitDataValue)
                        .putString(PERIOD_ID, periodIdDataValue)
                        .putString(ATTRIBUTE_OPTION_COMBO, attributeComboDataValue)
                        .build();
        }
        String uid = recordUid;
        if (dataToDataValues == null) {
            syncGranularEventBuilder.setInputData(new Data.Builder().putString(UID, recordUid).putString(CONFLICT_TYPE, conflictTypeData.name()).build());
        } else {
            syncGranularEventBuilder.setInputData(dataToDataValues);
            uid = orgUnitDataValue + "_" + periodIdDataValue + "_" + attributeComboDataValue;
        }
        OneTimeWorkRequest request = syncGranularEventBuilder.build();
        WorkManager.getInstance(getContext().getApplicationContext()).beginUniqueWork(uid, ExistingWorkPolicy.KEEP, request).enqueue();
        WorkManager.getInstance(getContext().getApplicationContext()).getWorkInfosForUniqueWorkLiveData(uid)
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.size() > 0)
                        manageWorkInfo(workInfo.get(0));
                });
    }

    private void manageWorkInfo(WorkInfo workInfo) {
        binding.synsStatusRecycler.setVisibility(View.VISIBLE);
        switch (workInfo.getState()) {
            case ENQUEUED:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    binding.syncIcon.setImageResource(R.drawable.animator_sync_grey);
                    if (binding.syncIcon.getDrawable() instanceof AnimatedVectorDrawable)
                        ((AnimatedVectorDrawable) binding.syncIcon.getDrawable()).start();
                }
                adapter.addItem(StatusLogItem.create(Calendar.getInstance().getTime(),
                        getString(R.string.start_sync_granular)));
                break;
            case RUNNING:
                adapter.addItem(StatusLogItem.create(Calendar.getInstance().getTime(),
                        getString(R.string.syncing)));
                break;
            case SUCCEEDED:
                processor.onNext(true);
                binding.syncButton.setVisibility(View.GONE);
                adapter.addItem(StatusLogItem.create(Calendar.getInstance().getTime(),
                        getString(R.string.end_sync_granular)));
                binding.noConflictMessage.setText(getString(R.string.no_conflicts_synced_message));
                Bindings.setStateIcon(binding.syncIcon, State.SYNCED);
                break;
            case FAILED:
                List<StatusLogItem> listStatusLog = new ArrayList<>();
                if (workInfo.getOutputData().getKeyValueMap().get("conflict") != null) {
                    for (String tracker : workInfo.getOutputData().getStringArray("conflict")) {
                        try {
                            listStatusLog.add(StatusLogItem.create(DateUtils.getInstance().databaseDateFormat().parse(tracker.split("/")[0])
                                    , tracker.split("/")[1]));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    adapter.addAllItems(listStatusLog);
                } else {
                    adapter.addItem(StatusLogItem.create(Calendar.getInstance().getTime(),
                            getString(R.string.error_sync)));
                }
                Bindings.setStateIcon(binding.syncIcon, State.ERROR);
                processor.onNext(false);
                break;
            case BLOCKED:
                break;
            case CANCELLED:
                adapter.addItem(StatusLogItem.create(Calendar.getInstance().getTime(),
                        getString(R.string.cancel_sync)));
                break;
            default:
                break;
        }

    }
}
