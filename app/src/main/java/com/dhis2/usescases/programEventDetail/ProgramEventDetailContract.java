package com.dhis2.usescases.programEventDetail;

import com.dhis2.usescases.general.AbstractActivityContracts;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

/**
 * Created by Cristian on 13/02/2017.
 *
 */

public class ProgramEventDetailContract {

    public interface View extends AbstractActivityContracts.View {
        void setData(List<EventModel> events);

        void addTree(TreeNode treeNode);

        void openDrawer();

        void showTimeUnitPicker();

        void showRageDatePicker();

        void setProgram(ProgramModel programModel);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view, String programId);

        void onTimeButtonClick();

        void onDateRangeButtonClick();

        void onOrgUnitButtonClick();

        void onCatComboButtonClick();

        ProgramModel getCurrentProgram();

        void onSearchClick();

        void onBackClick();

        void onTEIClick(String TEIuid, String programUid);

        void setProgram(ProgramModel program);
    }

    public interface Interactor extends AbstractActivityContracts.Interactor {
        void init(View view, String programId);
    }
}
