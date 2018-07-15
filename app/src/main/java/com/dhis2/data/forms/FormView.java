package com.dhis2.data.forms;

import android.support.annotation.NonNull;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.dhis2.data.tuples.Pair;
import com.dhis2.data.tuples.Trio;
import com.google.android.gms.maps.model.LatLng;

import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

interface FormView {

    @NonNull
    Observable<ReportStatus> eventStatusChanged();

    @NonNull
    Observable<String> reportDateChanged();

    @NonNull
    Observable<String> incidentDateChanged();

    @NonNull
    Observable<LatLng> reportCoordinatesChanged();

    @NonNull
    Consumer<List<FormSectionViewModel>> renderSectionViewModels();

    @NonNull
    Consumer<String> renderReportDate();

    @NonNull
    Consumer<String> renderTitle();

    @NonNull
    Consumer<ReportStatus> renderStatus();

    @NonNull
    Consumer<Trio<String, String, String>> finishEnrollment();

    void renderStatusChangeSnackBar(@NonNull ReportStatus eventStatus);

    @NonNull
    Consumer<Pair<ProgramModel, String>> renderIncidentDate();

    void initReportDatePicker(boolean reportAllowFutureDates, boolean incidentAllowFutureDates);

    void onNext(ReportStatus reportStatus);

    void isMandatoryFieldsRequired(List<FieldViewModel> viewModels);

    void showMandatoryFieldsDialog();

    void onAllSavedDataDeleted();

    void onBackPressed();

    void messageOnComplete(String content, boolean b);
}