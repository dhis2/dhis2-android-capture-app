package com.dhis2.Bindings;

import android.databinding.BindingAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by ppajuelo on 28/09/2017.
 */

public class Bindings {

    @BindingAdapter("initGrid")
    public static void setLayoutManager(RecyclerView recyclerView, boolean addLayout) {
        recyclerView.setLayoutManager(new GridLayoutManager(recyclerView.getContext(), 4));
    }

}
