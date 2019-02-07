package org.dhis2.usescases.main.program;

import android.database.Cursor;
import androidx.databinding.BaseObservable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        static final String TRACKED_ENTITY = "trackedEntity";
        static final String DISPLAY_FRONT_PAGE_LIST = "displayFrontPageList";
        static final String CATEGORY_COMBO = "categoryCombo";
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

    @Nullable
    @ColumnName(Columns.TRACKED_ENTITY)
    public abstract String trackedEntityType();

    @Nullable
    @ColumnName(Columns.DISPLAY_FRONT_PAGE_LIST)
    public abstract String displayFrontPageList();

    @Nullable
    @ColumnName(Columns.CATEGORY_COMBO)
    public abstract String categoryCombo();

    @NonNull
    public static HomeViewModel create(@NonNull String id, @NonNull String title, @NonNull String lastUpdated,
                                       @NonNull Type type, @NonNull String programType, @Nullable String trackedEntityType, @Nullable String displayFrontPageList,
                                       @Nullable String categoryCombo) {
        return new AutoValue_HomeViewModel(id, title, lastUpdated, type, programType, trackedEntityType, displayFrontPageList, categoryCombo);
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
