package com.dhis2.usescases.main;

import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Observable;

interface HomeRepository {

    @NonNull
    Observable<List<HomeViewModel>> homeViewModels();
}