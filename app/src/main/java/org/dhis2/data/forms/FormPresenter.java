package org.dhis2.data.forms;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;

interface FormPresenter {

    @UiThread
    void onAttach(@NonNull FormView view);

    @UiThread
    void onDetach();

    void checkSections();

    void checkMandatoryFields();

    void deleteCascade();

    void saveCategoryOption(CategoryOptionComboModel selectedOption);

    void initializeSaveObservable();

    void getNeedInitial(String eventUid);
}