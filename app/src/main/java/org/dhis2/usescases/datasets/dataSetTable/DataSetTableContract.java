package org.dhis2.usescases.datasets.dataSetTable;

import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSection;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.validationrules.Violation;

import java.util.List;

import io.reactivex.Observable;

public class DataSetTableContract {

    public interface View extends AbstractActivityContracts.View {

        void startInputEdition();

        void finishInputEdition();

        void setSections(List<DataSetSection> sections, String sectionToOpenUid);

        Boolean accessDataWrite();

        String getDataSetUid();

        void renderDetails(DataSetRenderDetails dataSetRenderDetails);

        void showMandatoryMessage(boolean isMandatoryFields);

        void showValidationRuleDialog();

        void showSuccessValidationDialog();

        void savedAndCompleteMessage();

        void showErrorsValidationDialog(List<Violation> violations);

        void showCompleteToast();

        void collapseExpandBottom();

        void closeBottomSheet();

        void completeBottomSheet();

        void displayReopenedMessage(boolean done);

        void showInternalValidationError();

        void saveAndFinish();

        boolean isErrorBottomSheetShowing();

        void selectOpenedSection(int sectionIndexToOpen);
    }
}
