package com.dhis2.utils;

import android.support.annotation.NonNull;

import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.functions.Consumer;

/**
 * Created by ppajuelo on 12/02/2018.
 */

public class OnErrorHandler implements Consumer<Throwable> {

    @NonNull
    public static Consumer<Throwable> create() {
        return new OnErrorHandler();
    }

    private OnErrorHandler() {
        // use factory method
    }

    @Override
    public void accept(Throwable throwable) {
        throw new OnErrorNotImplementedException(throwable);
    }
}
