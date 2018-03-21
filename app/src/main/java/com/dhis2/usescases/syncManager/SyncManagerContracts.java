package com.dhis2.usescases.syncManager;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.dhis2.usescases.general.AbstractActivityContracts;
import com.dhis2.usescases.main.MainContracts;

import io.reactivex.functions.Consumer;

/**
 * Created by lmartin on 21/03/2018.
 */

public class SyncManagerContracts {

    interface View extends AbstractActivityContracts.View {


    }

    public interface Presenter {

        void sync();

    }
}
