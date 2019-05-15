package org.dhis2.usescases.main.program;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.dhis2.App;
import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.databinding.SyncBottomDialogBinding;
import org.dhis2.utils.NetworkUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.hisp.dhis.android.core.program.ProgramType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static org.hisp.dhis.android.core.utils.support.StringUtils.isEmpty;

public class SyncStatusDialog extends BottomSheetDialogFragment {

    private final String programUid;
    private final CompositeDisposable compositeDisposable;
    private D2 d2;
    private SyncBottomDialogBinding binding;

    public SyncStatusDialog(String programUid) {
        this.programUid = programUid;
        this.compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        d2 = ((App) context.getApplicationContext()).serverComponent().userManager().getD2();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.sync_bottom_dialog, container, false);

        compositeDisposable.add(
                Observable.fromCallable(() -> d2.programModule().programs.uid(programUid).get())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                program -> {
                                    binding.programName.setText(program.displayName());
                                },
                                error -> dismiss()
                        )
        );

        compositeDisposable.add(
                Observable.fromCallable(() -> d2.importModule().trackerImportConflicts.get())
                        .map(conflicts -> {
                            Iterator<TrackerImportConflict> iterator = conflicts.iterator();
                            while (iterator.hasNext()) {
                                TrackerImportConflict conflict = iterator.next();
                                if (!isEmpty(conflict.enrollment())) {
                                    if (d2.enrollmentModule().enrollments.uid(conflict.enrollment()).get().program().equals(programUid))
                                        iterator.remove();
                                } else if (!isEmpty(conflict.trackedEntityInstance())) {
                                    List<String> programs = new ArrayList<>();
                                    programs.add(programUid);
                                    if (d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programs).byUid().eq(conflict.trackedEntityInstance()).get().isEmpty())
                                        iterator.remove();
                                } else if (!isEmpty(conflict.event())) {
                                    if (d2.eventModule().events.uid(conflict.event()).get().program().equals(programUid))
                                        iterator.remove();
                                } else
                                    iterator.remove();
                            }
                            return conflicts;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                conflicts -> {
                                    if (conflicts.isEmpty())
                                        setNoConflictMessage();
                                    else
                                        prepareConflictAdapter();
                                },
                                error -> dismiss()
                        )
        );

        compositeDisposable.add(
                Observable.fromCallable(() -> d2.programModule().programs.uid(programUid).get())
                        .map(program -> {
                            State state = State.SYNCED;
                            if (program.programType() == ProgramType.WITH_REGISTRATION) {
                                List<String> programUids = new ArrayList<>();
                                programUids.add(program.uid());
                                if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.ERROR, State.WARNING).get().isEmpty())
                                    state = State.WARNING;
                                else if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).get().isEmpty())
                                    state = State.SENT_VIA_SMS;
                                else if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.TO_UPDATE, State.TO_POST, State.TO_DELETE).get().isEmpty())
                                    state = State.TO_UPDATE;
                            } else {
                                if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.ERROR, State.WARNING).get().isEmpty())
                                    state = State.WARNING;
                                else if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).get().isEmpty())
                                    state = State.SENT_VIA_SMS;
                                else if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.TO_UPDATE, State.TO_POST, State.TO_DELETE).get().isEmpty())
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
                                    binding.syncStatusBar.setBackgroundColor(getColorForState(state));
                                },
                                error -> dismiss()
                        )
        );


        return binding.getRoot();
    }

    private void setNetworkMessage() {
        if (!NetworkUtils.isOnline(getContext())) {
            if (/*Check SMS Services*/true) {
                binding.connectionMessage.setText("Network connection is not available, but it looks like there is phone services - you can use SMS.");
                binding.syncButton.setText(R.string.action_sync_sms);
                binding.syncButton.setVisibility(View.VISIBLE);

            } else {
                binding.connectionMessage.setText("Network connection is not available.");
                binding.syncButton.setVisibility(View.INVISIBLE);
            }
        } else {
            binding.connectionMessage.setText(null);
            binding.syncButton.setText(R.string.action_sync);
            binding.syncButton.setVisibility(View.VISIBLE);
        }

    }

    private void prepareConflictAdapter() {
        binding.syncButton.setVisibility(View.VISIBLE);
        binding.synsStatusRecycler.setVisibility(View.VISIBLE);
        binding.noConflictMessage.setVisibility(View.GONE);
        //TODO: Create adapter
        setNetworkMessage();

    }

    private void setNoConflictMessage() {
        binding.syncButton.setVisibility(View.INVISIBLE);
        binding.synsStatusRecycler.setVisibility(View.GONE);
        binding.noConflictMessage.setVisibility(View.VISIBLE);
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
            case TO_DELETE:
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
            case TO_DELETE:
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
        super.onDismiss(dialog);
    }
}
