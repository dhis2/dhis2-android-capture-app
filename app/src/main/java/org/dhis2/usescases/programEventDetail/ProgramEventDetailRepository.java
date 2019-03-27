package org.dhis2.usescases.programEventDetail;

import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.program.Program;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import io.reactivex.Observable;

/**
 * Created by Cristian E. on 02/11/2017.
 */

public interface ProgramEventDetailRepository {

    @NonNull
    LiveData<PagedList<ProgramEventViewModel>> filteredProgramEvents(List<DatePeriod> dateFilter, List<String> orgUnitFilter, List<CategoryOptionCombo> catOptionComboUid);

    @NonNull
    Observable<Program> program();

    @NonNull
    Observable<List<Category>> catCombo();

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits();

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits(String parentUid);


    boolean getAccessDataWrite();

    List<CategoryOptionCombo> catOptionCombo(List<CategoryOption> selectedOptions);
}
