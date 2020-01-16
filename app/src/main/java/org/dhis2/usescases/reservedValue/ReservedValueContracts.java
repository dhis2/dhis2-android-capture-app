package org.dhis2.usescases.reservedValue;

import org.dhis2.usescases.general.AbstractActivityContracts;

import java.util.List;

public class ReservedValueContracts {

    public interface View extends AbstractActivityContracts.View {
        void setReservedValues(List<ReservedValueModel> reservedValueModels);

        void onBackClick();

        void showReservedValuesError();
    }
}
