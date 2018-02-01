package com.dhis2.usescases.enrollment;

import com.dhis2.usescases.general.AbstractActivityContracts;

/**
 * Created by ppajuelo on 31/01/2018.
 */

public class EnrollmentContracts {

    interface View extends AbstractActivityContracts.View {

    }

    interface Presenter {
        void init(String programUid);
        void deAttach();

    }
}
