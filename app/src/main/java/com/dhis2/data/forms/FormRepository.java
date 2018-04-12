package com.dhis2.data.forms;

import android.support.annotation.NonNull;

import org.hisp.dhis.rules.RuleEngine;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public interface FormRepository {

    @NonNull
    Flowable<String> title();

    @NonNull
    Flowable<String> reportDate();

    @NonNull
    Flowable<RuleEngine> ruleEngine();

    @NonNull
    Consumer<String> storeReportDate();

    @NonNull
    Flowable<ReportStatus> reportStatus();

    @NonNull
    Flowable<List<FormSectionViewModel>> sections();

    @NonNull
    Consumer<ReportStatus> storeReportStatus();

    @NonNull
    Consumer<String> autoGenerateEvent();

    @NonNull
    Observable<String> useFirstStageDuringRegistration();
}