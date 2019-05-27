package org.dhis2.usescases.teiDashboard.teiProgramList;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import org.dhis2.R;
import org.dhis2.usescases.main.program.ProgramViewModel;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.custom_views.OrgUnitDialog;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by Cristian on 06/03/2018.
 */

public class TeiProgramListInteractor implements TeiProgramListContract.Interactor {

    private TeiProgramListContract.View view;
    private String trackedEntityId;
    private CompositeDisposable compositeDisposable;
    private final TeiProgramListRepository teiProgramListRepository;
    private Date selectedEnrollmentDate;

    TeiProgramListInteractor(TeiProgramListRepository teiProgramListRepository) {
        this.teiProgramListRepository = teiProgramListRepository;
    }

    @Override
    public void init(TeiProgramListContract.View view, String trackedEntityId) {
        this.view = view;
        this.trackedEntityId = trackedEntityId;
        compositeDisposable = new CompositeDisposable();

        getActiveEnrollments();
        getOtherEnrollments();
        getPrograms();
    }

    private void showNativeCalendar(String programUid, String uid, OrgUnitDialog orgUnitDialog) {

        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dateDialog = new DatePickerDialog(view.getContext(), (
                (datePicker, year1, month1, day1) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(Calendar.YEAR, year1);
                    selectedCalendar.set(Calendar.MONTH, month1);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, day1);
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    selectedCalendar.set(Calendar.MINUTE, 0);
                    selectedCalendar.set(Calendar.SECOND, 0);
                    selectedCalendar.set(Calendar.MILLISECOND, 0);
                    selectedEnrollmentDate = selectedCalendar.getTime();
                    String enrollmentDate = DateUtils.uiDateFormat().format(selectedEnrollmentDate);

                    compositeDisposable.add(getOrgUnits(programUid)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    allOrgUnits -> {
                                        ArrayList<OrganisationUnitModel> orgUnits = new ArrayList<>();
                                        for (OrganisationUnitModel orgUnit : allOrgUnits) {
                                            boolean afterOpening = false;
                                            boolean beforeClosing = false;
                                            if (orgUnit.openingDate() == null || !selectedEnrollmentDate.before(orgUnit.openingDate()))
                                                afterOpening = true;
                                            if (orgUnit.closedDate() == null || !selectedEnrollmentDate.after(orgUnit.closedDate()))
                                                beforeClosing = true;
                                            if (afterOpening && beforeClosing)
                                                orgUnits.add(orgUnit);
                                        }
                                        if (orgUnits.size() > 1) {
                                            orgUnitDialog.setOrgUnits(orgUnits);
                                            if (!orgUnitDialog.isAdded())
                                                orgUnitDialog.show(view.getAbstracContext().getSupportFragmentManager(), "OrgUnitEnrollment");
                                        } else
                                            enrollInOrgUnit(orgUnits.get(0).uid(), programUid, uid, selectedEnrollmentDate);
                                    },
                                    Timber::d
                            )
                    );


                }),
                year,
                month,
                day);
        ProgramModel selectedProgram = getProgramFromUid(programUid);
        if (selectedProgram != null && !selectedProgram.selectEnrollmentDatesInFuture()) {
            dateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }
        if (selectedProgram != null) {
            dateDialog.setTitle(selectedProgram.enrollmentDateLabel());
        }
        dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, view.getContext().getString(R.string.date_dialog_clear), (dialog, which) -> {
            dialog.dismiss();
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            dateDialog.setButton(DialogInterface.BUTTON_NEUTRAL, view.getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> {
                dateDialog.dismiss();
                showCustomCalendar(programUid, uid, orgUnitDialog);
            });
        }

        dateDialog.show();
    }

    private void showCustomCalendar(String programUid, String uid, OrgUnitDialog orgUnitDialog) {
        LayoutInflater layoutInflater = LayoutInflater.from(view.getContext());
        View datePickerView = layoutInflater.inflate(R.layout.widget_datepicker, null);
        final DatePicker datePicker = datePickerView.findViewById(R.id.widget_datepicker);

        Calendar c = Calendar.getInstance();
        datePicker.updateDate(
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext(), R.style.DatePickerTheme)
                .setPositiveButton(R.string.action_accept, (dialog, which) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(Calendar.YEAR, datePicker.getYear());
                    selectedCalendar.set(Calendar.MONTH, datePicker.getMonth());
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    selectedCalendar.set(Calendar.MINUTE, 0);
                    selectedCalendar.set(Calendar.SECOND, 0);
                    selectedCalendar.set(Calendar.MILLISECOND, 0);
                    selectedEnrollmentDate = selectedCalendar.getTime();
                    String enrollmentDate = DateUtils.uiDateFormat().format(selectedEnrollmentDate);

                    compositeDisposable.add(getOrgUnits(programUid)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    allOrgUnits -> {
                                        ArrayList<OrganisationUnitModel> orgUnits = new ArrayList<>();
                                        for (OrganisationUnitModel orgUnit : allOrgUnits) {
                                            boolean afterOpening = false;
                                            boolean beforeClosing = false;
                                            if (orgUnit.openingDate() == null || !selectedEnrollmentDate.before(orgUnit.openingDate()))
                                                afterOpening = true;
                                            if (orgUnit.closedDate() == null || !selectedEnrollmentDate.after(orgUnit.closedDate()))
                                                beforeClosing = true;
                                            if (afterOpening && beforeClosing)
                                                orgUnits.add(orgUnit);
                                        }
                                        if (orgUnits.size() > 1) {
                                            orgUnitDialog.setOrgUnits(orgUnits);
                                            if (!orgUnitDialog.isAdded())
                                                orgUnitDialog.show(view.getAbstracContext().getSupportFragmentManager(), "OrgUnitEnrollment");
                                        } else
                                            enrollInOrgUnit(orgUnits.get(0).uid(), programUid, uid, selectedEnrollmentDate);
                                    },
                                    Timber::d
                            )
                    );
                })
                .setNeutralButton(view.getContext().getResources().getString(R.string.change_calendar),
                        (dialog, which) -> showNativeCalendar(programUid, uid, orgUnitDialog))
                .setNegativeButton(view.getContext().getString(R.string.date_dialog_clear), (dialog, which) -> {
                    dialog.dismiss();
                });

        ProgramModel selectedProgram = getProgramFromUid(programUid);
        if (selectedProgram != null && !selectedProgram.selectEnrollmentDatesInFuture()) {
            datePicker.setMaxDate(System.currentTimeMillis());
        }

        if (selectedProgram != null) {
            alertDialog.setTitle(selectedProgram.enrollmentDateLabel());
        }

        alertDialog.setView(datePickerView);
        Dialog dialog = alertDialog.create();
        dialog.show();
    }

    @Override
    public void enroll(String programUid, String uid) {
        selectedEnrollmentDate = Calendar.getInstance().getTime();

        OrgUnitDialog orgUnitDialog = OrgUnitDialog.getInstace().setMultiSelection(false);
        orgUnitDialog.setTitle("Enrollment Org Unit")
                .setPossitiveListener(v -> {
                    if (orgUnitDialog.getSelectedOrgUnit() != null && !orgUnitDialog.getSelectedOrgUnit().isEmpty())
                        enrollInOrgUnit(orgUnitDialog.getSelectedOrgUnit(), programUid, uid, selectedEnrollmentDate);
                    orgUnitDialog.dismiss();
                })
                .setNegativeListener(v -> orgUnitDialog.dismiss());

        showCustomCalendar(programUid, uid, orgUnitDialog);
    }

    private ProgramModel getProgramFromUid(String programUid) {
        return teiProgramListRepository.getProgram(programUid);
    }

    private void enrollInOrgUnit(String orgUnitUid, String programUid, String teiUid, Date enrollmentDate) {
        compositeDisposable.add(
                teiProgramListRepository.saveToEnroll(orgUnitUid, programUid, teiUid, enrollmentDate)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(enrollmentUid -> {
                                    view.goToEnrollmentScreen(enrollmentUid, programUid);
                                },
                                Timber::d)
        );
    }

    public Observable<List<OrganisationUnitModel>> getOrgUnits(String programUid) {
        return teiProgramListRepository.getOrgUnits(programUid);
    }

    private void getActiveEnrollments() {
        compositeDisposable.add(teiProgramListRepository.activeEnrollments(trackedEntityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setActiveEnrollments,
                        Timber::d)
        );
    }

    private void getOtherEnrollments() {
        compositeDisposable.add(teiProgramListRepository.otherEnrollments(trackedEntityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setOtherEnrollments,
                        Timber::d)
        );
    }

    private void getPrograms() {
        compositeDisposable.add(teiProgramListRepository.allPrograms(trackedEntityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::getAlreadyEnrolledPrograms,
                        Timber::d)
        );
    }

    private void getAlreadyEnrolledPrograms(List<ProgramViewModel> programs) {
        compositeDisposable.add(teiProgramListRepository.alreadyEnrolledPrograms(trackedEntityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        alreadyEnrolledPrograms -> deleteRepeatedPrograms(programs, alreadyEnrolledPrograms),
                        Timber::d)
        );
    }

    private void deleteRepeatedPrograms(List<ProgramViewModel> allPrograms, List<ProgramModel> alreadyEnrolledPrograms) {
        ArrayList<ProgramViewModel> programListToPrint = new ArrayList<>();
        for (ProgramViewModel programModel1 : allPrograms) {
            boolean isAlreadyEnrolled = false;
            boolean onlyEnrollOnce = false;
            for (ProgramModel programModel2 : alreadyEnrolledPrograms) {
                if (programModel1.id().equals(programModel2.uid())) {
                    isAlreadyEnrolled = true;
                    onlyEnrollOnce = programModel2.onlyEnrollOnce();
                }
            }
            if (!isAlreadyEnrolled || !onlyEnrollOnce) {
                programListToPrint.add(programModel1);
            }
        }
        view.setPrograms(programListToPrint);
    }

    @Override
    public String getProgramColor(String uid) {
        return teiProgramListRepository.getProgramColor(uid);
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }
}
