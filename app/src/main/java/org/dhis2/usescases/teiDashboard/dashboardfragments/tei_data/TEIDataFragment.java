package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.FragmentTeiDataBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionActivity;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardViewModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.EventCreationType;
import org.dhis2.utils.ObjectStyleUtils;
import org.dhis2.utils.customviews.CategoryComboDialog;
import org.dhis2.utils.customviews.CustomDialog;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.functions.Consumer;

import static android.app.Activity.RESULT_OK;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static org.dhis2.utils.Constants.ENROLLMENT_UID;
import static org.dhis2.utils.Constants.EVENT_CREATION_TYPE;
import static org.dhis2.utils.Constants.EVENT_SCHEDULE_INTERVAL;
import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PROGRAM_UID;
import static org.dhis2.utils.Constants.TRACKED_ENTITY_INSTANCE;
import static org.dhis2.utils.analytics.AnalyticsConstants.CREATE_EVENT_TEI;
import static org.dhis2.utils.analytics.AnalyticsConstants.TYPE_EVENT_TEI;

/**
 * -Created by ppajuelo on 29/11/2017.
 */

public class TEIDataFragment extends FragmentGlobalAbstract implements TEIDataContracts.View {

    private static final int REQ_DETAILS = 1001;
    private static final int REQ_EVENT = 2001;

    private static final int RC_GENERATE_EVENT = 1501;
    private static final int RC_EVENTS_COMPLETED = 1601;


    private FragmentTeiDataBinding binding;

    @Inject
    TEIDataContracts.Presenter presenter;

    private EventAdapter adapter;
    private CustomDialog dialog;
    private String lastModifiedEventUid;
    private ProgramStage programStageFromEvent;
    private ObservableBoolean followUp = new ObservableBoolean(false);

    private boolean hasCatComb;
    private ArrayList<Event> catComboShowed = new ArrayList<>();
    private Context context;
    private DashboardViewModel dashboardViewModel;
    private DashboardProgramModel dashboardModel;
    private TeiDashboardMobileActivity activity;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
        activity = (TeiDashboardMobileActivity) context;
        if (((App) context.getApplicationContext()).dashboardComponent() != null)
            ((App) context.getApplicationContext())
                    .dashboardComponent()
                    .plus(new TEIDataModule(this, activity.getProgramUid(), activity.getTeiUid()))
                    .inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        dashboardViewModel = ViewModelProviders.of(activity).get(DashboardViewModel.class);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tei_data, container, false);
        binding.setPresenter(presenter);

        binding.fab.setOptionsClick(integer -> {
            if (integer == null)
                return;
            switch (integer) {
                case R.id.referral:
                    createEvent(EventCreationType.REFERAL, 0);
                    break;
                case R.id.addnew:
                    createEvent(EventCreationType.ADDNEW, 0);
                    break;
                case R.id.schedulenew:
                    createEvent(EventCreationType.SCHEDULE, 0);
                    break;
                default:
                    break;
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.init();
        dashboardViewModel.dashboardModel().observe(this, this::setData);
        dashboardViewModel.eventUid().observe(this, this::displayGenerateEvent);
    }

    @Override
    public void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    public void setData(DashboardProgramModel nprogram) {
        this.dashboardModel = nprogram;

        if (nprogram != null && nprogram.getCurrentEnrollment() != null) {
            presenter.setDashboardProgram(this.dashboardModel);
            SharedPreferences prefs = context.getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE);
            hasCatComb = nprogram.getCurrentProgram() != null && !nprogram.getCurrentProgram().categoryComboUid().equals(prefs.getString(Constants.DEFAULT_CAT_COMBO, ""));
            adapter = new EventAdapter(presenter, nprogram.getProgramStages(), new ArrayList<>(), nprogram.getCurrentEnrollment(), nprogram.getCurrentProgram());
            binding.teiRecycler.setLayoutManager(new LinearLayoutManager(getAbstracContext()));
            binding.teiRecycler.setAdapter(adapter);
            binding.setTrackEntity(nprogram.getTei());
            binding.setEnrollment(nprogram.getCurrentEnrollment());
            binding.setProgram(nprogram.getCurrentProgram());
            binding.setDashboardModel(nprogram);
            presenter.getTEIEvents();
            followUp.set(nprogram.getCurrentEnrollment().followUp() != null ? nprogram.getCurrentEnrollment().followUp() : false);
            binding.setFollowup(followUp);

        } else if (nprogram != null) {
            binding.fab.setVisibility(View.GONE);
            binding.teiRecycler.setLayoutManager(new LinearLayoutManager(getAbstracContext()));
            binding.teiRecycler.setAdapter(new DashboardProgramAdapter(presenter, nprogram));
            binding.teiRecycler.addItemDecoration(new DividerItemDecoration(getAbstracContext(), DividerItemDecoration.VERTICAL));
            binding.setTrackEntity(nprogram.getTei());
            binding.setEnrollment(null);
            binding.setProgram(null);
            binding.setDashboardModel(nprogram);
            binding.setFollowup(followUp);
        }

        binding.executePendingBindings();

        if (getSharedPreferences().getString("COMPLETED_EVENT", null) != null) {
            presenter.displayGenerateEvent(getSharedPreferences().getString("COMPLETED_EVENT", null));
            getSharedPreferences().edit().remove("COMPLETED_EVENT").apply();
        }


    }

    @SuppressLint("CheckResult")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       /* if (requestCode == REQ_DETAILS) {
            if (resultCode == RESULT_OK) {
                activity.getPresenter().getData();
            }
        }*/
        if (requestCode == REQ_EVENT && resultCode == RESULT_OK) {
            presenter.getTEIEvents();
            if (data != null) {
                lastModifiedEventUid = data.getStringExtra(Constants.EVENT_UID);
                if (((TeiDashboardMobileActivity) context).getOrientation() != Configuration.ORIENTATION_LANDSCAPE)
                    getSharedPreferences().edit().putString("COMPLETED_EVENT", lastModifiedEventUid).apply();
                else {
                    if (lastModifiedEventUid != null)
                        presenter.displayGenerateEvent(lastModifiedEventUid);
                }
            }

        }
    }

    @Override
    public Consumer<List<Event>> setEvents() {
        return events -> {
            if (events.isEmpty()) {
                binding.emptyTeis.setVisibility(View.VISIBLE);
                if (binding.fab.getVisibility() == View.VISIBLE) {
                    binding.emptyTeis.setText(R.string.empty_tei_add);
                } else {
                    binding.emptyTeis.setText(R.string.empty_tei_no_add);
                }
            } else {
                binding.emptyTeis.setVisibility(View.GONE);
                adapter.swapItems(events);
                for (Event event : events) {
                    if (event.eventDate() != null) {
                        if (event.eventDate().after(DateUtils.getInstance().getToday()))
                            binding.teiRecycler.scrollToPosition(events.indexOf(event));
                    }
                    if (hasCatComb && event.attributeOptionCombo() == null && !catComboShowed.contains(event)) {
                        presenter.getCatComboOptions(event);
                        catComboShowed.add(event);
                    } else if (!hasCatComb && event.attributeOptionCombo() == null)
                        presenter.setDefaultCatOptCombToEvent(event.uid());
                }
            }
        };
    }

    @Override
    public Consumer<ProgramStage> displayGenerateEvent() {
        return programStageModel -> {
            this.programStageFromEvent = programStageModel;
            if (programStageModel.displayGenerateEventBox() || programStageModel.allowGenerateNextVisit()) {
                dialog = new CustomDialog(
                        getContext(),
                        getString(R.string.dialog_generate_new_event),
                        getString(R.string.message_generate_new_event),
                        getString(R.string.button_ok),
                        getString(R.string.cancel),
                        RC_GENERATE_EVENT,
                        new DialogClickListener() {
                            @Override
                            public void onPositive() {
                                createEvent(EventCreationType.SCHEDULE, programStageFromEvent.standardInterval() != null ? programStageFromEvent.standardInterval() : 0);
                            }

                            @Override
                            public void onNegative() {
                                if (programStageFromEvent.remindCompleted())
                                    presenter.areEventsCompleted();
                            }
                        });
                dialog.show();
            } else if (programStageModel.remindCompleted())
                showDialogCloseProgram();
        };
    }

    private void showDialogCloseProgram() {

        dialog = new CustomDialog(
                getContext(),
                getString(R.string.event_completed),
                getString(R.string.complete_enrollment_message),
                getString(R.string.button_ok),
                getString(R.string.cancel),
                RC_EVENTS_COMPLETED,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        presenter.completeEnrollment();
                    }

                    @Override
                    public void onNegative() {
                    }
                });
        dialog.show();
    }

    @Override
    public Consumer<Single<Boolean>> areEventsCompleted() {
        return eventsCompleted -> {
            if (eventsCompleted.blockingGet()) {
                dialog = new CustomDialog(
                        getContext(),
                        getString(R.string.event_completed_title),
                        getString(R.string.event_completed_message),
                        getString(R.string.button_ok),
                        getString(R.string.cancel),
                        RC_EVENTS_COMPLETED,
                        new DialogClickListener() {
                            @Override
                            public void onPositive() {
                                presenter.completeEnrollment();
                            }

                            @Override
                            public void onNegative() {
                            }
                        });
                dialog.show();
            }

        };
    }

    @Override
    public Consumer<EnrollmentStatus> enrollmentCompleted() {
        return enrollmentStatus -> {
            if (enrollmentStatus == EnrollmentStatus.COMPLETED)
                activity.getPresenter().getData();
        };
    }

    private void createEvent(EventCreationType eventCreationType, Integer scheduleIntervalDays) {
        if (isAdded()) {
            analyticsHelper().setEvent(TYPE_EVENT_TEI, eventCreationType.name(), CREATE_EVENT_TEI);
            Bundle bundle = new Bundle();
            bundle.putString(PROGRAM_UID, dashboardModel.getCurrentEnrollment().program());
            bundle.putString(TRACKED_ENTITY_INSTANCE, dashboardModel.getTei().uid());
            bundle.putString(ORG_UNIT, dashboardModel.getTei().organisationUnit()); //We take the OU of the TEI for the events
            bundle.putString(ENROLLMENT_UID, dashboardModel.getCurrentEnrollment().uid());
            bundle.putString(EVENT_CREATION_TYPE, eventCreationType.name());
            bundle.putInt(EVENT_SCHEDULE_INTERVAL, scheduleIntervalDays);
            Intent intent = new Intent(getContext(), ProgramStageSelectionActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQ_EVENT);
        }
    }

    @Override
    public void showCatComboDialog(String eventId, CategoryCombo categoryCombo, List<CategoryOptionCombo> categoryOptionCombos) {
        CategoryComboDialog dialog = new CategoryComboDialog(
                getAbstracContext(),
                categoryCombo,
                123,
                selectedOption ->
                        presenter.changeCatOption(
                                eventId,
                                selectedOption),
                categoryCombo.displayName());

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public void switchFollowUp(boolean followUp) {
        this.followUp.set(followUp);
    }

    @Override
    public void displayGenerateEvent(String eventUid) {
        if (eventUid != null) {
            presenter.displayGenerateEvent(eventUid);
            dashboardViewModel.updateEventUid(null);
        }
    }

    @Override
    public void restoreAdapter(String programUid) {
        activity.restoreAdapter(programUid);
    }

    @Override
    public void seeDetails(Intent intent, Bundle bundle) {
        this.startActivity(intent, bundle);
    }

    @Override
    public void showQR(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void openEventDetails(Intent intent, Bundle bundle) {
        this.startActivityForResult(intent, REQ_EVENT, bundle);
    }

    @Override
    public void openEventInitial(Intent intent) {
        this.startActivityForResult(intent, REQ_EVENT, null);
    }

    @Override
    public void openEventCapture(Intent intent) {
        this.startActivityForResult(intent, REQ_EVENT, null);
    }

    @Override
    public void showTeiImage(String filePath, String defaultIcon) {
        Glide.with(this)
                .load(new File(filePath))
                .placeholder(
                        ObjectStyleUtils.getIconResource(context, defaultIcon, R.drawable.photo_temp_gray)
                )
                .error(
                        ObjectStyleUtils.getIconResource(context, defaultIcon, R.drawable.photo_temp_gray)
                )
                .transition(withCrossFade())
                .transform(new CircleCrop())
                .into(binding.cardFront.teiImage);
    }
}