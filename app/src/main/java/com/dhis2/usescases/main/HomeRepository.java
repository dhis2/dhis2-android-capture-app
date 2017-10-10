package com.dhis2.usescases.main;

import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 10/10/2017.
 */

interface HomeRepository {
    @NonNull
    Observable<List<HomeViewModel>> homeViewModels();
}
