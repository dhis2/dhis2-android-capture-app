package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators;

import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.program.ProgramIndicator;

import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
public class IndicatorsContracts {

    public interface View extends AbstractActivityContracts.View {

        Consumer<List<Trio<ProgramIndicator, String, String>>> swapIndicators();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {

        void init(View view);

    }

}
