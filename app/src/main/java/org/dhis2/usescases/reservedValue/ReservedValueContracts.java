package org.dhis2.usescases.reservedValue;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.trackedentity.ReservedValueSummary;

import java.util.List;

public class ReservedValueContracts {

    public interface View extends AbstractActivityContracts.View {
        void setReservedValues(List<ReservedValueSummary> reservedValueModels);

        void onBackClick();

        void showReservedValuesError();
    }
}
