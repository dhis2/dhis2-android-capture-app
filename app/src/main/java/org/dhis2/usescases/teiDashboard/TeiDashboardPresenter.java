package org.dhis2.usescases.teiDashboard;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity;
import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.IndicatorsFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.notes.NotesFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.notes.NotesPresenter;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipPresenter;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataFragment;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataPresenter;
import org.dhis2.usescases.teiDashboard.eventDetail.EventDetailActivity;
import org.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import org.dhis2.usescases.teiDashboard.teiDataDetail.TeiDataDetailActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.EventCreationType;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.program.ProgramIndicatorModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.RelationshipHelper;
import org.hisp.dhis.android.core.relationship.RelationshipItem;
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance;
import org.hisp.dhis.android.core.relationship.RelationshipModel;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair;
import org.hisp.dhis.rules.models.RuleActionDisplayText;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */

public class TeiDashboardPresenter implements TeiDashboardContracts.Presenter, TEIDataPresenter,
        RelationshipPresenter, NotesPresenter {

    private final DashboardRepository dashboardRepository;
    private final MetadataRepository metadataRepository;
    private final D2 d2;
    private TeiDashboardContracts.View view;

    private String teUid;
    private String teType;
    private String programUid;
    private boolean programWritePermission;

    private CompositeDisposable compositeDisposable;
    private DashboardProgramModel dashboardProgramModel;

    private MutableLiveData<DashboardProgramModel> dashboardProgramModelLiveData = new MutableLiveData<>();

    public TeiDashboardPresenter(D2 d2, DashboardRepository dashboardRepository, MetadataRepository metadataRepository) {
        this.d2 = d2;
        this.dashboardRepository = dashboardRepository;
        this.metadataRepository = metadataRepository;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public LiveData<DashboardProgramModel> observeDashboardModel() {
        return dashboardProgramModelLiveData;
    }

    @Override
    public void init(TeiDashboardContracts.View view, String teiUid, String programUid) {
        this.view = view;
        this.teUid = teiUid;
        this.programUid = programUid;

        dashboardRepository.setDashboardDetails(teiUid, programUid);

        getData();
    }

    @SuppressLint({"CheckResult"})
    @Override
    public void getData() {
        if (programUid != null)
            compositeDisposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(teUid),
                    dashboardRepository.getEnrollment(programUid, teUid),
                    dashboardRepository.getProgramStages(programUid),
                    dashboardRepository.getTEIEnrollmentEvents(programUid, teUid),
                    metadataRepository.getProgramTrackedEntityAttributes(programUid),
                    dashboardRepository.getTEIAttributeValues(programUid, teUid),
                    metadataRepository.getTeiOrgUnit(teUid, programUid),
                    metadataRepository.getTeiActivePrograms(teUid),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            dashboardModel -> {
                                this.dashboardProgramModel = dashboardModel;
                                this.dashboardProgramModelLiveData.setValue(dashboardModel);
                                this.programWritePermission = dashboardProgramModel.getCurrentProgram().accessDataWrite();
                                this.teType = dashboardProgramModel.getTei().trackedEntityType();
                                view.setData(dashboardProgramModel);
                            },
                            Timber::e
                    )
            );

        else {
            compositeDisposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(teUid),
                    metadataRepository.getProgramTrackedEntityAttributes(null),
                    dashboardRepository.getTEIAttributeValues(null, teUid),
                    metadataRepository.getTeiOrgUnit(teUid),
                    metadataRepository.getTeiActivePrograms(teUid),
                    metadataRepository.getTEIEnrollments(teUid),
                    DashboardProgramModel::new)
                    .flatMap(dashboardProgramModel1 -> metadataRepository.getObjectStylesForPrograms(dashboardProgramModel1.getEnrollmentProgramModels())
                            .map(stringObjectStyleMap -> {
                                dashboardProgramModel1.setProgramsObjectStyles(stringObjectStyleMap);
                                return dashboardProgramModel1;
                            }))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            dashboardModel -> {
                                this.dashboardProgramModel = dashboardModel;
                                this.teType = dashboardProgramModel.getTei().trackedEntityType();
                                view.setData(dashboardProgramModel);
                            },
                            Timber::e)
            );
        }
    }

    @Override
    public DashboardProgramModel getDashBoardData() {
        return dashboardProgramModel;
    }

    @Override
    public void getTEIEvents(TEIDataFragment teiFragment) {
        compositeDisposable.add(
                dashboardRepository.getTEIEnrollmentEvents(programUid, teUid)
                        .map(eventModels -> {
                            for (EventModel eventModel : eventModels) {
                                if (eventModel.status() == EventStatus.SCHEDULE && eventModel.dueDate() != null && eventModel.dueDate().before(DateUtils.getInstance().getToday())) { //If a schedule event dueDate is before today the event is skipped
                                    dashboardRepository.updateState(eventModel, EventStatus.SKIPPED);
                                }
                            }
                            return eventModels;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                teiFragment.setEvents(),
                                Timber::d
                        )
        );
    }


    @Override
    public void areEventsCompleted(TEIDataFragment teiDataFragment) {
        compositeDisposable.add(
                dashboardRepository.getEnrollmentEventsWithDisplay(programUid, teUid)
                        .flatMap(events -> events.isEmpty() ? dashboardRepository.getTEIEnrollmentEvents(programUid, teUid) : Observable.just(events))
                        .map(events -> Observable.fromIterable(events).all(event -> event.status() == EventStatus.COMPLETED))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                teiDataFragment.areEventsCompleted(),
                                Timber::d
                        )
        );
    }

    @Override
    public void displayGenerateEvent(TEIDataFragment teiDataFragment, String eventUid) {
        compositeDisposable.add(
                dashboardRepository.displayGenerateEvent(eventUid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                teiDataFragment.displayGenerateEvent(),
                                Timber::d
                        )
        );
    }

    @Override
    public void generateEvent(String lastModifiedEventUid, Integer standardInterval) {
        compositeDisposable.add(
                dashboardRepository.generateNewEvent(lastModifiedEventUid, standardInterval)
                        .take(1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> view.displayMessage(result),
                                Timber::d
                        )
        );
    }

    @Override
    public void generateEventFromDate(String lastModifiedEventUid, Calendar chosenDate) {
        compositeDisposable.add(
                dashboardRepository.generateNewEventFromDate(lastModifiedEventUid, chosenDate)
                        .take(1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> view.displayMessage(result),
                                Timber::d
                        )
        );
    }


    @Override
    public void onShareClick(View mView) {
        PopupMenu menu = new PopupMenu(view.getContext(), mView);

        menu.getMenu().add(Menu.NONE, Menu.NONE, 0, "QR");
        //menu.getMenu().add(Menu.NONE, Menu.NONE, 1, "SMS"); TODO: When SMS is ready, reactivate option

        menu.setOnMenuItemClickListener(item -> {
            switch (item.getOrder()) {
                case 0:
                    view.showQR();
                    return true;
                case 1:
                    view.displayMessage(view.getContext().getString(R.string.feature_unavaible));
                    return true;
                default:
                    return true;
            }
        });

        menu.show();
    }

    @Override
    public void onEnrollmentSelectorClick() {
        Bundle extras = new Bundle();
        extras.putString("TEI_UID", teUid);
        view.goToEnrollmentList(extras);
    }

    @Override
    public void onShareQRClick() {
        view.showQR();
    }

    @Override
    public void setProgram(ProgramModel program) {
        this.programUid = program.uid();
        view.restoreAdapter(programUid);
        getData();
    }

    @Override
    public void seeDetails(View sharedView, DashboardProgramModel dashboardProgramModel) {
        Fragment teiFragment = TEIDataFragment.getInstance();
        Intent intent = new Intent(view.getContext(), TeiDataDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putString("TEI_UID", teUid);
        extras.putString("PROGRAM_UID", programUid);
        if (dashboardProgramModel.getCurrentEnrollment() != null)
            extras.putString("ENROLLMENT_UID", dashboardProgramModel.getCurrentEnrollment().uid());
        intent.putExtras(extras);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "user_info");
        teiFragment.startActivityForResult(intent, TEIDataFragment.getDetailsRequestCode(), options.toBundle());
    }

    @Override
    public void onEventSelected(String uid, View sharedView) {
        Fragment teiFragment = TEIDataFragment.getInstance();
        if (teiFragment != null && teiFragment.getContext() != null && teiFragment.isAdded()) {
            Intent intent = new Intent(teiFragment.getContext(), EventInitialActivity.class);
            intent.putExtras(EventInitialActivity.getBundle(
                    programUid, uid, EventCreationType.DEFAULT.name(), teUid, null, null, null, dashboardProgramModel.getCurrentEnrollment().uid(), 0, dashboardProgramModel.getCurrentEnrollment().enrollmentStatus()
            ));
            teiFragment.startActivityForResult(intent, TEIDataFragment.getEventRequestCode(), null);
        }
    }

    @Override
    public void onScheduleSelected(String uid, View sharedView) {
        Fragment teiFragment = TEIDataFragment.getInstance();
        if (teiFragment != null && teiFragment.getContext() != null && teiFragment.isAdded()) {
            Intent intent = new Intent(teiFragment.getContext(), EventDetailActivity.class);
            Bundle extras = new Bundle();
            extras.putString("EVENT_UID", uid);
            extras.putString("TOOLBAR_TITLE", view.getToolbarTitle());
            extras.putString("TEI_UID", teUid);
            intent.putExtras(extras);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(view.getAbstractActivity(), sharedView, "shared_view");
            teiFragment.startActivityForResult(intent, TEIDataFragment.getEventRequestCode(), options.toBundle());
        }
    }

    @Override
    public void onFollowUp(DashboardProgramModel dashboardProgramModel) {
        boolean followup = dashboardRepository.setFollowUp(dashboardProgramModel.getCurrentEnrollment().uid());


        view.showToast(followup ?
                view.getContext().getString(R.string.follow_up_enabled) :
                view.getContext().getString(R.string.follow_up_disabled));

        TEIDataFragment.getInstance().switchFollowUp(followup);


    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    public Observable<List<TrackedEntityAttributeValueModel>> getTEIMainAttributes(String teiUid) {
        return dashboardRepository.mainTrackedEntityAttributes(teiUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    @Override
    public void goToAddRelationship(String teiTypeToAdd) {
        if (programWritePermission) {
            Fragment relationshipFragment = RelationshipFragment.getInstance();
            Intent intent = new Intent(view.getContext(), SearchTEActivity.class);
            Bundle extras = new Bundle();
            extras.putBoolean("FROM_RELATIONSHIP", true);
            extras.putString("FROM_RELATIONSHIP_TEI", teUid);
            extras.putString("TRACKED_ENTITY_UID", teiTypeToAdd);
            extras.putString("PROGRAM_UID", null);
            intent.putExtras(extras);
            relationshipFragment.startActivityForResult(intent, Constants.REQ_ADD_RELATIONSHIP);
        } else
            view.displayMessage(view.getContext().getString(R.string.search_access_error));
    }

    @Override
    public void addRelationship(String trackEntityInstance_A, String relationshipType) {
        try {
            Relationship relationship = RelationshipHelper.teiToTeiRelationship(teUid, trackEntityInstance_A, relationshipType);
            d2.relationshipModule().relationships.add(relationship);
        } catch (D2Error e) {
            view.displayMessage(e.errorDescription());
        }
    }


    @Override
    public void deleteRelationship(Relationship relationship) {
        try {
            d2.relationshipModule().relationships.withAllChildren().uid(relationship.uid()).delete();
        } catch (D2Error e) {
            Timber.d(e);
        } finally {
            subscribeToRelationships(RelationshipFragment.getInstance());
        }
    }

    @Override
    public void subscribeToRelationships(RelationshipFragment relationshipFragment) {
        compositeDisposable.add(
                Flowable.just(
                        d2.relationshipModule().relationships.getByItem(
                                RelationshipItem.builder().trackedEntityInstance(
                                        RelationshipItemTrackedEntityInstance.builder().trackedEntityInstance(teUid).build()).build()
                        ))
                        .flatMapIterable(list -> list)
                        .filter(relationship -> relationship.from().trackedEntityInstance().trackedEntityInstance().equals(teUid))
                        .map(relationship -> {
                            RelationshipType relationshipType = null;
                            for (RelationshipType type : d2.relationshipModule().relationshipTypes.get())
                                if (type.uid().equals(relationship.relationshipType()))
                                    relationshipType = type;
                            return Pair.create(relationship, relationshipType);
                        })
                        .toList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                relationshipFragment.setRelationships(),
                                Timber::d
                        )
        );
    }


    @Override
    public void subscribeToRelationshipTypes(RelationshipFragment relationshipFragment) {
        compositeDisposable.add(
                dashboardRepository.relationshipsForTeiType(teType)
                        .map(list -> {
                            List<Trio<RelationshipTypeModel, String, Integer>> finalList = new ArrayList<>();
                            for (Pair<RelationshipTypeModel, String> rType : list) {
                                int iconResId = dashboardRepository.getObjectStyle(view.getAbstracContext(), rType.val1());
                                finalList.add(Trio.create(rType.val0(), rType.val1(), iconResId));
                            }
                            return finalList;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                RelationshipFragment.getInstance().setRelationshipTypes(),
                                Timber::e
                        )
        );
    }

    private void applyRuleEffects(Result<RuleEffect> calcResult, IndicatorsFragment indicatorsFragment) {

        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            return;
        }

        for (RuleEffect ruleEffect : calcResult.items()) {
            RuleAction ruleAction = ruleEffect.ruleAction();
            if (ruleAction instanceof RuleActionDisplayKeyValuePair) {
                Trio<ProgramIndicatorModel, String, String> indicator = Trio.create(
                        ProgramIndicatorModel.builder().displayName(((RuleActionDisplayKeyValuePair) ruleAction).content()).build(),
                        ruleEffect.data(), "");
                indicatorsFragment.addIndicator(indicator);
            } else if (ruleAction instanceof RuleActionDisplayText) {
                Trio<ProgramIndicatorModel, String, String> indicator = Trio.create(
                        ProgramIndicatorModel.builder().displayName(((RuleActionDisplayText) ruleAction).content()).build(),
                        ruleEffect.data(), "");
                indicatorsFragment.addIndicator(indicator);
            }
        }
    }


    @Override
    public void onDescriptionClick(String description) {
        view.showDescription(description);
    }


    @Override
    public void setNoteProcessor(Flowable<Pair<String, Boolean>> noteProcessor) {
        compositeDisposable.add(noteProcessor
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        dashboardRepository.handleNote(),
                        Timber::d
                ));
    }

    @Override
    public void subscribeToNotes(NotesFragment notesFragment) {
        compositeDisposable.add(dashboardRepository.getNotes(programUid, teUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        notesFragment.swapNotes(),
                        Timber::d
                )
        );
    }

    @Override
    public void openDashboard(String teiUid) {
        Intent intent = new Intent(view.getContext(), TeiDashboardMobileActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("TEI_UID", teiUid);
        bundle.putString("PROGRAM_UID", null);
        intent.putExtras(bundle);
        view.getAbstractActivity().startActivity(intent);
    }

    @Override
    public void subscribeToRelationshipLabel(RelationshipModel relationship, TextView textView) {

       /*
        compositeDisposable.add(
                metadataRepository.getRelationshipTypeList()
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMapIterable(data -> data)
                        .filter(relationshipTypeModel -> relationshipTypeModel.uid().equals(relationship.relationshipType()))
                        .map(item -> {
                            if (teUid.equals(relationship.trackedEntityInstanceA()))
                                return item.bIsToA();
                            else
                                return item.aIsToB();
                        })
                        .subscribe(
                                textView::setText,
                                t -> view.displayMessage(view.getContext().getString(R.string.relationship_label_error)))

        );*/
    }

    @Override
    public void onBackPressed() {
        view.back();
    }

    @Override
    public String getTeUid() {
        return teUid;
    }

    @Override
    public String getProgramUid() {
        return programUid;
    }

    @Override
    public Boolean hasProgramWritePermission() {
        return programWritePermission;
    }

    @Override
    public void completeEnrollment(TEIDataFragment teiDataFragment) {
        if (programWritePermission) {
            Flowable<Long> flowable = null;
            EnrollmentStatus newStatus = EnrollmentStatus.COMPLETED;

            flowable = dashboardRepository.updateEnrollmentStatus(dashboardProgramModel.getCurrentEnrollment().uid(), newStatus);
            compositeDisposable.add(flowable
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(result -> newStatus)
                    .subscribe(
                            teiDataFragment.enrollmentCompleted(),
                            Timber::d
                    )
            );
        } else
            view.displayMessage(null);
    }

    @Override
    public void showDescription(String description) {
        view.showDescription(description);
    }

    public void getCatComboOptions(EventModel event) {
        compositeDisposable.add(
                dashboardRepository.catComboForProgram(event.program())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(catCombo -> {
                                    for (ProgramStageModel programStage : dashboardProgramModel.getProgramStages()) {
                                        if (event.programStage().equals(programStage.uid()))
                                            view.showCatComboDialog(event.uid(), catCombo);
                                    }
                                },
                                Timber::e));

       /* compositeDisposable.add(
                Observable.zip(
                        metadataRepository.getCategoryComboOptions(dashboardProgramModel.getCurrentProgram().categoryCombo()),
                        metadataRepository.getCategoryFromCategoryCombo(dashboardProgramModel.getCurrentProgram().categoryCombo()),
                        Pair::create
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(pair -> {
                                    for (ProgramStageModel programStage : dashboardProgramModel.getProgramStages()) {
                                        if (event.programStage().equals(programStage.uid()))
                                            view.showCatComboDialog(event.uid(), pair.val1().displayName(), pair.val0(), programStage.displayName());
                                    }
                                },
                                Timber::e));*/
    }

    @Override
    public void changeCatOption(String eventUid, String catOptionComboUid) {
        metadataRepository.saveCatOption(eventUid, catOptionComboUid);
    }

    @Override
    public void setDefaultCatOptCombToEvent(String eventUid) {
        dashboardRepository.setDefaultCatOptCombToEvent(eventUid);
    }
}