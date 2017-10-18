package com.dhis2.usescases.main.program;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.gabrielittner.auto.value.cursor.ColumnAdapter;
import com.gabrielittner.auto.value.cursor.ColumnName;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class HomeViewModel {

    static class Columns {
        static final String UID = "uid";
        static final String DISPLAY_NAME = "displayName";
        static final String HOME_VIEW_MODEL_TYPE = "homeViewModelType";
    }

    @NonNull
    @ColumnName(Columns.UID)
    public abstract String id();

    @NonNull
    @ColumnName(Columns.DISPLAY_NAME)
    public abstract String title();

    @NonNull
    @ColumnName(Columns.HOME_VIEW_MODEL_TYPE)
    @ColumnAdapter(HomeViewModelTypeColumnAdapter.class)
    public abstract Type type();

    @NonNull
    public static HomeViewModel create(@NonNull String id, @NonNull String title,
                                       @NonNull Type type) {
        return new AutoValue_HomeViewModel(id, title, type);
    }

    public static HomeViewModel fromCursor(Cursor cursor) {
        return AutoValue_HomeViewModel.createFromCursor(cursor);
    }

    public enum Type {
        PROGRAM,
        TRACKED_ENTITY,
        UNKNOWN
    }


}
