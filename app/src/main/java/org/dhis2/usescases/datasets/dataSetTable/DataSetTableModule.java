package org.dhis2.usescases.datasets.dataSetTable;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialRepository;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialRepositoryImpl;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import kotlin.Unit;
import kotlinx.coroutines.flow.Flow;

@Module
public class DataSetTableModule {

    private DataSetTableContract.View view;
    private final String dataSetUid;
    private final String periodId;
    private final String orgUnitUid;
    private final String catOptCombo;

    DataSetTableModule(DataSetTableActivity view, String dataSetUid, String periodId, String orgUnitUid, String catOptCombo) {
        this.view = view;
        this.dataSetUid = dataSetUid;
        this.periodId = periodId;
        this.orgUnitUid = orgUnitUid;
        this.catOptCombo = catOptCombo;
    }

    @Provides
    @PerActivity
    DataSetTableContract.Presenter providesPresenter(
            DataSetTableRepositoryImpl dataSetTableRepository,
            SchedulerProvider schedulerProvider,
            AnalyticsHelper analyticsHelper,
            FlowableProcessor<Unit> updateProcessor) {
        return new DataSetTablePresenter(view, dataSetTableRepository, schedulerProvider, analyticsHelper, updateProcessor);
    }

    @Provides
    @PerActivity
    DataSetTableRepositoryImpl DataSetTableRepository(D2 d2) {
        return new DataSetTableRepositoryImpl(d2, dataSetUid, periodId, orgUnitUid, catOptCombo);
    }

    @Provides
    @PerActivity
    DataSetInitialRepository DataSetInitialRepository(D2 d2) {
        return new DataSetInitialRepositoryImpl(d2, dataSetUid);
    }

    @Provides
    @PerActivity
    FlowableProcessor<Unit> updateProcessor(){
        return PublishProcessor.create();
    }

}
