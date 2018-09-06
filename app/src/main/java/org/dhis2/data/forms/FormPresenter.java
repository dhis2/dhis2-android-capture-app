package org.dhis2.data.forms;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

interface FormPresenter {

    @UiThread
    void onAttach(@NonNull FormView view);

    @UiThread
    void onDetach();

    void checkSections();

    void checkMandatoryFields();

    void deleteCascade();
}