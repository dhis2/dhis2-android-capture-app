package com.dhis2.data.forms.dataentry;


import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

interface DataEntryPresenter {
    @UiThread
    void onAttach(@NonNull DataEntryView view);

    @UiThread
    void onDetach();

}
