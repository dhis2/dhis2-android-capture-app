package org.dhis2.usescases.searchTrackEntity;

import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel;
import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeModel;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class SearchTEContractsModule {

    public interface View extends AbstractActivityContracts.View {
        void setForm(List<TrackedEntityAttribute> trackedEntityAttributeModels, @Nullable Program program, HashMap<String, String> queryData);

        void setPrograms(List<Program> programModels);

        void clearList(String uid);

        Flowable<RowAction> rowActionss();

        Flowable<Trio<String, String, Integer>> optionSetActions();

        void clearData();

        void setTutorial();

        void setProgramColor(String data);

        String fromRelationshipTEI();

        void setListOptions(List<OptionModel> options);

        void setLiveData(LiveData<PagedList<SearchTeiModel>> liveData);

        void setFabIcon(boolean needsSearch);
    }

    public interface Presenter {

        void init(View view, String trackedEntityType, String initialProgram);

        void onDestroy();

        void setProgram(Program programSelected);

        void onBackClick();

        void onClearClick();

        void onFabClick(android.view.View view, boolean needsSearch);

        void onEnrollClick(android.view.View view);

        void enroll(String programUid, String uid);

        void onTEIClick(String TEIuid, boolean isOnline);

        TrackedEntityTypeModel getTrackedEntityName();

        Program getProgram();

        void addRelationship(String TEIuid, String relationshipTypeUid, boolean online);

        void addRelationship(String TEIuid, boolean online);

        void downloadTei(String teiUid);

        void downloadTeiForRelationship(String TEIuid, String relationshipTypeUid);

        Observable<List<OrganisationUnit>> getOrgUnits();

        String getProgramColor(String uid);

        Trio<PagedList<SearchTeiModel>, String, Boolean> getMessage(PagedList<SearchTeiModel> list);

        HashMap<String, String> getQueryData();

        void initSearch(SearchTEContractsModule.View view);

        void onSyncIconClick(String teiUid);
    }
}
