package com.dhis2.usescases.main.home;

import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Observable;

public interface HomeRepository {

    @NonNull
    Observable<List<HomeViewModel>> homeViewModels();
}