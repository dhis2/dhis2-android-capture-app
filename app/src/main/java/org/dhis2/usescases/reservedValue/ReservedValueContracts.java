package org.dhis2.usescases.reservedValue;

import org.dhis2.usescases.general.AbstractActivityContracts;

import java.util.List;

public class ReservedValueContracts {

    interface View extends AbstractActivityContracts.View {
        void setDataElements(List<ReservedValueModel> reservedValueModels);

        void onBackClick();

        void refreshAdapter();

        void showReservedValuesError();
    }

    public interface Presenter {
        void init(ReservedValueContracts.View view);

        void onClickRefill(ReservedValueModel reservedValue);

        void onBackClick();

        void onPause();
    }
}
