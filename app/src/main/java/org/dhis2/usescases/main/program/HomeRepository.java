package org.dhis2.usescases.main.program;

import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.DatePeriod;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Observable;

interface HomeRepository {

    @NonNull
    Flowable<List<ProgramViewModel>> programModels(List<DatePeriod> dateFilter, List<String> orgUnitFilter);

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits(String parentUid);

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits();

    @NonNull
    Flowable<List<ProgramViewModel>> aggregatesModels(List<DatePeriod> dateFilter, List<String> orgUnitFilter);
}