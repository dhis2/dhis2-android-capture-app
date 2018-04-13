package com.dhis2.usescases.searchTrackEntity;

import android.app.DatePickerDialog;
import android.support.annotation.Nullable;

import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.usescases.general.AbstractActivityContracts;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeModel;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class SearchTEContractsModule {

    public interface View extends AbstractActivityContracts.View {
        void setForm(List<TrackedEntityAttributeModel> trackedEntityAttributeModels, @Nullable ProgramModel program);

        void showDateDialog(DatePickerDialog.OnDateSetListener listener);

        Consumer<List<TrackedEntityInstanceModel>> swapListData();

        void setPrograms(List<ProgramModel> programModels);

        void clearList(String uid);

        android.view.View getProgress();

        Flowable<RowAction> rowActionss();
    }

    public interface Presenter {

        void init(SearchTEContractsModule.View view, String trackedEntityType);

        void onDestroy();

        void query(String format, boolean isAttribute);

        void setProgram(ProgramModel programSelected);

        void onBackClick();

        void onClearClick();

        void onEnrollClick(android.view.View view);

        void enroll(String programUid, String uid);

        void onTEIClick(String TEIuid);

        TrackedEntityTypeModel getTrackedEntityName();

        ProgramModel getProgramModel();

        List<ProgramModel> getProgramList();
    }
}
