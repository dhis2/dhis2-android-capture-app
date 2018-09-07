package org.dhis2.utils;

import android.support.annotation.NonNull;

import java.util.Date;

final class CurrentDateProviderImpl implements CurrentDateProvider {

    @NonNull
    @Override
    public Date currentDate() {
        return new Date();
    }
}
