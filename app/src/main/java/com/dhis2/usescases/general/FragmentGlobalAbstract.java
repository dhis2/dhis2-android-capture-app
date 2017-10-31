package com.dhis2.usescases.general;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;

/**
 * Created by ppajuelo on 18/10/2017.
 */

public class FragmentGlobalAbstract extends android.support.v4.app.Fragment implements AbstractActivityContracts.View {
    public ViewDataBinding binding;


    @Override
    public void startActivity(@NonNull Class<?> destination, @Nullable Bundle bundle, boolean finishCurrent, boolean finishAll, @Nullable ActivityOptionsCompat transition) {
        getAbstracContext().startActivity(destination, bundle, finishCurrent, finishAll, transition);
    }

    @Override
    public ActivityGlobalAbstract getAbstracContext() {
        return (ActivityGlobalAbstract) getActivity();
    }
}
