package com.dhis2.usescases.main.program;

import android.database.Cursor;
import android.databinding.BaseObservable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gabrielittner.auto.value.cursor.ColumnName;
import com.google.auto.value.AutoValue;

import java.io.Serializable;

@AutoValue
public abstract class ProgramViewModel extends BaseObservable implements Serializable {

    static class Columns {
        static final String UID = "uid";
        static final String DISPLAY_NAME = "displayName";
        static final String ICON = "icon";
        static final String COLOR = "homeViewModelType";
        static final String COUNT = "count";
        static final String TRACKED_ENTITY_TYPE = "trackedEntityName";
    }

    @NonNull
    @ColumnName(Columns.UID)
    public abstract String id();

    @NonNull
    @ColumnName(Columns.DISPLAY_NAME)
    public abstract String title();

    @NonNull
    @ColumnName(Columns.COLOR)
    public abstract String color();

    @NonNull
    @ColumnName(Columns.ICON)
    public abstract String icon();

    @NonNull
    @ColumnName(Columns.COUNT)
    public abstract Integer count();

    @NonNull
    @ColumnName(Columns.TRACKED_ENTITY_TYPE)
    public abstract String type();

    @NonNull
    public static ProgramViewModel create(@NonNull String uid, @NonNull String displayName, @Nullable String color,
                                          @Nullable String icon, @Nullable Integer count, @NonNull String type) {
        return new AutoValue_ProgramViewModel(uid, displayName, color, icon, count, type);
    }

    public static ProgramViewModel fromCursor(Cursor cursor) {
        return AutoValue_ProgramViewModel.createFromCursor(cursor);
    }

}
