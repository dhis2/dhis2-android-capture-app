package org.dhis2.utils;

import android.support.annotation.NonNull;

import java.util.Date;

public interface CurrentDateProvider {
    @NonNull
    Date currentDate();
}
