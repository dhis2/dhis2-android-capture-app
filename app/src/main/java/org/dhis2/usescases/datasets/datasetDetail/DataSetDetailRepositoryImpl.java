package org.dhis2.usescases.datasets.datasetDetail;

import androidx.annotation.NonNull;

import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration;
import org.hisp.dhis.android.core.dataset.DataSetInstanceCollectionRepository;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.Period;

import java.util.List;
import java.util.Locale;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public class DataSetDetailRepositoryImpl implements DataSetDetailRepository {

    private final D2 d2;

    public DataSetDetailRepositoryImpl(D2 d2) {
        this.d2 = d2;
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnit>> orgUnits() {
        return Observable.just(d2.organisationUnitModule().organisationUnits.blockingGet());
    }

    @Override
    public Flowable<List<DataSetDetailModel>> dataSetGroups(String dataSetUid, List<String> orgUnits, List<String> periodFilter, int page) {
        DataSetInstanceCollectionRepository repo;
        repo = d2.dataSetModule().dataSetInstances.byDataSetUid().eq(dataSetUid);
        if (!orgUnits.isEmpty())
            repo = repo.byOrganisationUnitUid().in(orgUnits);
        if (!periodFilter.isEmpty())
            repo = repo.byPeriod().in(periodFilter);

        DataSetInstanceCollectionRepository finalRepo = repo;
        return Flowable.fromIterable(finalRepo.blockingGet())
                .map(dataSetReport -> {
                    Period period = d2.periodModule().periods.byPeriodId().eq(dataSetReport.period()).one().get();
                    String periodName = DateUtils.getInstance().getPeriodUIString(period.periodType(), period.startDate(), Locale.getDefault());
                    DataSetCompleteRegistration dscr = d2.dataSetModule().dataSetCompleteRegistrations
                            .byDataSetUid().eq(dataSetUid)
                            .byAttributeOptionComboUid().eq(dataSetReport.attributeOptionComboUid())
                            .byOrganisationUnitUid().eq(dataSetReport.organisationUnitUid())
                            .byPeriod().eq(dataSetReport.period()).one().get();
                    State state;
                    if(dscr != null && dscr.state() != State.SYNCED) {
                        if (dscr.state() == State.TO_DELETE)
                            state = State.TO_UPDATE;
                        else
                            state = dscr.state();
                    }
                    else
                        state = dataSetReport.state();

                    return DataSetDetailModel.create(
                            dataSetReport.organisationUnitUid(),
                            dataSetReport.attributeOptionComboUid(),
                            dataSetReport.period(),
                            dataSetReport.organisationUnitDisplayName(),
                            dataSetReport.attributeOptionComboDisplayName(),
                            periodName,
                            state,
                            dataSetReport.periodType().name());
                })
                .toList()
                .toFlowable();
    }
}