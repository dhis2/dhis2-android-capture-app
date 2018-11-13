package org.dhis2.usescases.reservedValue;

import org.dhis2.usescases.general.AbstractActivityContracts;

import java.util.List;

public class ReservedValueContracts {

    interface View extends AbstractActivityContracts.View {
        void setDataElements(List<ReservedValueModel> reservedValueModels);
    }

    public interface Presenter {
        void init(ReservedValueContracts.View view);
    }
}
