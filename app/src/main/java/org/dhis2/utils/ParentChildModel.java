package org.dhis2.utils;

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * QUADRAM. Created by ppajuelo on 27/11/2018.
 */
@AutoValue
public abstract class ParentChildModel<T> {

    @NonNull
    public abstract T parent();

    @NonNull
    public abstract List<ParentChildModel<T>> childs();

    public abstract boolean isSelectable();

    @NonNull
    public abstract Integer numberOfChilds();

    @NonNull
    public static <T> ParentChildModel<T> create(@NonNull T parent, @Nullable List<ParentChildModel<T>> childs, boolean isSelectable) {
        int count = 0;
        if (childs == null) {
            childs = new ArrayList<>();
        }
        for (ParentChildModel<T> child : childs)
            count += child.numberOfChilds();

        return new AutoValue_ParentChildModel<>(parent, childs, isSelectable, count + childs.size());
    }

    public void addItem(ParentChildModel<T> item) {
        childs().add(item);
    }
}
