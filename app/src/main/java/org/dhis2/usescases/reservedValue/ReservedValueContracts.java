package org.dhis2.usescases.reservedValue;

import org.dhis2.usescases.general.AbstractActivityContracts;

import java.util.List;

public class ReservedValueContracts {

    interface ReservedValueView extends AbstractActivityContracts.View {
        void setDataElements(List<ReservedValueModel> reservedValueModels);

        void onBackClick();

        void refreshAdapter();
    }

    public interface ReservedValuePresenter {
        void init(ReservedValueView view);

        void onClickRefill(ReservedValueModel reservedValue);

        void onBackClick();
    }
}
