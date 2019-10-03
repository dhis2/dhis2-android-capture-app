package org.dhis2.usescases.main.program;

import androidx.annotation.UiThread;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 18/10/2017.
 */
public class ProgramContract {

    interface View extends AbstractActivityContracts.View {

        Consumer<List<ProgramViewModel>> swapProgramModelData();

        @UiThread
        void renderError(String message);

        void openOrgUnitTreeSelector();

        void showHideFilter();

        void clearFilters();
    }

    public interface Presenter {
        void init(View view);

        void onItemClick(ProgramViewModel programModel);

        void showDescription(String description);

        List<OrganisationUnit> getOrgUnits();

        void dispose();

        void onSyncStatusClick(ProgramViewModel program);

        boolean areFiltersApplied();

        void showHideFilterClick();

        void clearFilterClick();
    }
}
