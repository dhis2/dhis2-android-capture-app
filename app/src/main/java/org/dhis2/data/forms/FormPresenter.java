package org.dhis2.data.forms;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import org.hisp.dhis.android.core.category.CategoryOptionCombo;

interface FormPresenter {

    String getEnrollmentOu(String enrollmentUid);

    @UiThread
    void onAttach(@NonNull FormView view);

    @UiThread
    void onDetach();

    void checkSections();

    void checkMandatoryFields();

    void deleteCascade();

    void saveCategoryOption(CategoryOptionCombo selectedOption);

    void initializeSaveObservable();

    void getNeedInitial(String eventUid);
}