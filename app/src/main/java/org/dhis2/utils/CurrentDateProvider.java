package org.dhis2.utils;

import androidx.annotation.NonNull;

import java.util.Date;

public interface CurrentDateProvider {
    @NonNull
    Date currentDate();
}
