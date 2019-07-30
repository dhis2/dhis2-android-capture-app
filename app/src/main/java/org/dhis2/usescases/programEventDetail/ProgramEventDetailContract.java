package org.dhis2.usescases.programEventDetail;

import com.unnamed.b.atv.model.TreeNode;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by Cristian on 13/02/2017.
 */

public class ProgramEventDetailContract {

    public interface View extends AbstractActivityContracts.View {

        void setProgram(Program programModel);

        void renderError(String message);

        void showHideFilter();

        void setWritePermission(Boolean aBoolean);

        void setLiveData(LiveData<PagedList<ProgramEventViewModel>> pagedListLiveData);

        void setOptionComboAccess(Boolean canCreateEvent);

        void updateFilters(int totalFilters);

        void setCatOptionComboFilter(Pair<CategoryCombo, List<CategoryOptionCombo>> categoryOptionCombos);

        void openOrgUnitTreeSelector();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view);

        void updateDateFilter(List<DatePeriod> datePeriodList);

        void addEvent();

        void onBackClick();

        void onEventClick(String eventId, String orgUnit);

        void showFilter();

        void onSyncIconClick(String uid);
    }
}
