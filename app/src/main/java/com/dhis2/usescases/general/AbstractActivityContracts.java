package com.dhis2.usescases.general;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;

import com.dhis2.usescases.programDetail.TrackedEntityObject;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.List;

/**
 * Created by ppajuelo on 27/09/2017.
 */

public class AbstractActivityContracts {

    public interface View {
        Context getContext();

        ActivityGlobalAbstract getAbstracContext();

        void startActivity(@NonNull Class<?> destination, @Nullable Bundle bundle, boolean finishCurrent, boolean finishAll, @Nullable ActivityOptionsCompat transition);

    }

    public interface Presenter {
        void onDettach();
    }

    public interface Interactor {
        void onDettach();
    }


}
