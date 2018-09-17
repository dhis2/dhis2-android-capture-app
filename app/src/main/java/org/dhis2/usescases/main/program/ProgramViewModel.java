package org.dhis2.usescases.main.program;

import android.database.Cursor;
import android.databinding.BaseObservable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gabrielittner.auto.value.cursor.ColumnName;
import com.google.auto.value.AutoValue;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@AutoValue
public abstract class ProgramViewModel extends BaseObservable implements Serializable {

    static class Columns {
        static final String UID = "uid";
        static final String DISPLAY_NAME = "displayName";
        static final String ICON = "icon";
        static final String COLOR = "homeViewModelType";
        static final String COUNT = "count";
        static final String TRACKED_ENTITY_TYPE = "trackedEntity";
        static final String TRACKED_ENTITY_TYPE_NAME = "trackedEntityName";
        static final String DESCRIPTION = "description";
        static final String PROGRAM_TYPE = "programType";
    }

    @NonNull
    @ColumnName(Columns.UID)
    public abstract String id();

    @NonNull
    @ColumnName(Columns.DISPLAY_NAME)
    public abstract String title();

    @Nullable
    @ColumnName(Columns.COLOR)
    public abstract String color();

    @Nullable
    @ColumnName(Columns.ICON)
    public abstract String icon();

    @NonNull
    @ColumnName(Columns.COUNT)
    public abstract Integer count();

    @Nullable
    @ColumnName(Columns.TRACKED_ENTITY_TYPE)
    public abstract String type();

    @NonNull
    @ColumnName(Columns.TRACKED_ENTITY_TYPE_NAME)
    public abstract String typeName();

    @NotNull
    @ColumnName(Columns.PROGRAM_TYPE)
    public abstract String programType();

    @Nullable
    @ColumnName(Columns.DESCRIPTION)
    public abstract String description();


    @NonNull
    public static ProgramViewModel create(@NonNull String uid, @NonNull String displayName, @Nullable String color,
                                          @Nullable String icon, @NonNull Integer count, @Nullable String type,
                                          @NonNull String typeName, @NonNull String programType, @Nullable String description) {
        return new AutoValue_ProgramViewModel(uid, displayName, color, icon, count, type, typeName, programType, description);
    }

    public static ProgramViewModel fromCursor(Cursor cursor) {
        return AutoValue_ProgramViewModel.createFromCursor(cursor);
    }

}
