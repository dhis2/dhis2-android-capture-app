package com.dhis2.usescases.searchTrackEntity;

import android.view.View;
import android.app.DatePickerDialog;
import android.support.annotation.Nullable;

import com.dhis2.usescases.general.AbstractActivityContracts;
import com.dhis2.usescases.programDetail.TrackedEntityObject;

import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */
public class SearchTEContractsModule {

    public interface View extends AbstractActivityContracts.View {
        void setForm(List<TrackedEntityAttributeModel> trackedEntityAttributeModels);

        void showDateDialog(DatePickerDialog.OnDateSetListener listener);

        void swapData(TrackedEntityObject body);

    }

    public interface Presenter {

        void init(SearchTEContractsModule.View view);

        void onDateClick(@Nullable DatePickerDialog.OnDateSetListener listener);

        Observable<List<OptionModel>> getOptions(String s);

        void query(String format);
    }

    public interface Interactor {
        void init(View view);

        void getTrackedEntityAttributes();

        void getProgramTrackedEntityAttributes();

        Observable<List<OptionModel>> getOptions(String optionSetId);

        void filterTrackEntities(String filter);
    }

    public interface Router {

    }
}
