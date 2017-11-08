package com.dhis2.usescases.main.program;

import android.database.Cursor;
import android.databinding.BaseObservable;
import android.support.annotation.NonNull;

import com.gabrielittner.auto.value.cursor.ColumnAdapter;
import com.gabrielittner.auto.value.cursor.ColumnName;
import com.google.auto.value.AutoValue;

import java.io.Serializable;

@AutoValue
public abstract class HomeViewModel extends BaseObservable implements Serializable {

    static class Columns {
        static final String UID = "uid";
        static final String DISPLAY_NAME = "displayName";
        static final String LAST_UPDATED = "lastUpdated";
        static final String HOME_VIEW_MODEL_TYPE = "homeViewModelType";
        static final String PROGRAM_TYPE = "programType";
        static final String PROGRAM_ATTRIBUTES = "programTrackedEntityAttributes";
    }

    @NonNull
    @ColumnName(Columns.UID)
    public abstract String id();

    @NonNull
    @ColumnName(Columns.DISPLAY_NAME)
    public abstract String title();

    @NonNull
    @ColumnName(Columns.LAST_UPDATED)
    public abstract String lastUpdated();

    @NonNull
    @ColumnName(Columns.HOME_VIEW_MODEL_TYPE)
    @ColumnAdapter(HomeViewModelTypeColumnAdapter.class)
    public abstract Type type();

    @NonNull
    @ColumnName(Columns.PROGRAM_TYPE)
    public abstract String programType();

   /* @NonNull
    @ColumnName(Columns.PROGRAM_ATTRIBUTES)
    public abstract List<ProgramTrackedEntityAttributeModel> programAttributes();*/

    @NonNull
    public static HomeViewModel create(@NonNull String id, @NonNull String title, @NonNull String lastUpdated,
                                       @NonNull Type type, @NonNull String programType) {
        return new AutoValue_HomeViewModel(id, title, lastUpdated, type, programType);
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
