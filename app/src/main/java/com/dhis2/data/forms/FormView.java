package com.dhis2.data.forms;

import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

interface FormView {

    @NonNull
    Observable<ReportStatus> eventStatusChanged();

    @NonNull
    Observable<String> reportDateChanged();

    @NonNull
    Consumer<List<FormSectionViewModel>> renderSectionViewModels();

    @NonNull
    Consumer<String> renderReportDate();

    @NonNull
    Consumer<String> renderTitle();

    @NonNull
    Consumer<ReportStatus> renderStatus();

    @NonNull
    Consumer<String> finishEnrollment();

    void renderStatusChangeSnackBar(@NonNull ReportStatus eventStatus);
}