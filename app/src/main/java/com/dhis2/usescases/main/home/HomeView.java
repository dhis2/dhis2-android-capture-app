package com.dhis2.usescases.main.home;

import android.support.annotation.UiThread;


import java.util.List;

import io.reactivex.functions.Consumer;

interface HomeView {

    Consumer<List<HomeViewModel>> swapData();

    @UiThread
    void renderError(String message);
}