package org.dhis2.usescases.teiDashboard.teiDataDetail;

import androidx.annotation.NonNull;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;

import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;

import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class TeiDataDetailContracts {

    public interface View extends AbstractActivityContracts.View {
        void init(String teUid, String programUid, String enrollmentUid);

        void setData(DashboardProgramModel program);

        Consumer<EnrollmentStatus> handleStatus();

        void showCustomIncidentCalendar(Date date);

        void showCustomEnrollmentCalendar(Date date);

        void showTeiImage(String fileName);

        Observable<Geometry> reportCoordinatesChanged();

        Observable<Geometry> teiCoordinatesChanged();

        @NonNull
        Observable<Unit> reportCoordinatesCleared();

        @NonNull
        Observable<Unit> teiCoordinatesCleared();

        Consumer<TrackedEntityType> renderTeiCoordinates();
    }

    public interface Presenter {
        void init(View view, String uid, String programUid, String enrollmentUid);

        void checkTeiCoordinates();

        void onBackPressed();

        void onDeactivate(DashboardProgramModel dashboardProgramModel);

        void onReOpen(DashboardProgramModel dashboardProgramModel);

        void onComplete(DashboardProgramModel dashboardProgramModel);

        void onActivate(DashboardProgramModel dashboardProgramModel);

        void onDestroy();

        void onIncidentDateClick(Date date);

        void onEnrollmentDateClick(Date date);

        void updateIncidentDate(Date date);

        void updateEnrollmentDate(Date date);

    }

}