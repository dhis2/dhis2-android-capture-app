package org.dhis2.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 27/11/2018.
 */
@AutoValue
public abstract class ParentChildModel<T> {

    @NonNull
    public abstract T parent();

    @NonNull
    public abstract List<ParentChildModel<T>> childs();

    @NonNull
    public abstract boolean isSelectable();

    @NonNull
    public abstract Integer numberOfChilds();

    @NonNull
    public static <T> ParentChildModel<T> create(@NonNull T parent, @Nullable List<ParentChildModel<T>> childs, boolean isSelectable) {
        int count = 0;
        for (ParentChildModel<T> child : childs)
            count += child.numberOfChilds();

        return new AutoValue_ParentChildModel<>(parent, childs != null ? childs : new ArrayList<>(), isSelectable, count + childs.size());
    }

    public void addItem(ParentChildModel<T> item) {
        childs().add(item);
    }

}
