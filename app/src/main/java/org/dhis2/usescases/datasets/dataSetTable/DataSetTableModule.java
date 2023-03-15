package org.dhis2.usescases.datasets.dataSetTable;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;

import org.dhis2.commons.di.dagger.PerActivity;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.viewmodel.DispatcherProvider;
import org.dhis2.data.dhislogic.DhisPeriodUtils;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialRepository;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialRepositoryImpl;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import kotlin.Unit;

@Module
public class DataSetTableModule {

    private final ViewModelStore viewModelStore;
    private DataSetTableContract.View view;
    private final String dataSetUid;
    private final String periodId;
    private final String orgUnitUid;
    private final String catOptCombo;

    private final boolean openErrorSectionOnInit;

    DataSetTableModule(
            DataSetTableActivity view,
            String dataSetUid,
            String periodId,
            String orgUnitUid,
            String catOptCombo,
            boolean openErrorSectionOnInit) {
        this.view = view;
        this.viewModelStore = view.getViewModelStore();
        this.dataSetUid = dataSetUid;
        this.periodId = periodId;
        this.orgUnitUid = orgUnitUid;
        this.catOptCombo = catOptCombo;
        this.openErrorSectionOnInit = openErrorSectionOnInit;
    }

    @Provides
    @PerActivity
    DataSetTablePresenter providesPresenter(
            DataSetTableRepositoryImpl dataSetTableRepository,
            DhisPeriodUtils periodUtils,
            AnalyticsHelper analyticsHelper,
            FlowableProcessor<Unit> updateProcessor,
            DispatcherProvider dispatcherProvider) {
        return new ViewModelProvider(
                viewModelStore,
                new DataSetTableViewModelFactory(
                        view,
                        dataSetTableRepository,
                        periodUtils,
                        analyticsHelper,
                        updateProcessor,
                        dispatcherProvider,
                        openErrorSectionOnInit
                )
        ).get(DataSetTablePresenter.class);
    }

    @Provides
    @PerActivity
    DataSetTableRepositoryImpl DataSetTableRepository(
            D2 d2,
            ResourceManager resourceManager) {
        return new DataSetTableRepositoryImpl(d2, dataSetUid, periodId, orgUnitUid, catOptCombo, resourceManager);
    }

    @Provides
    @PerActivity
    DataSetInitialRepository DataSetInitialRepository(D2 d2) {
        return new DataSetInitialRepositoryImpl(d2, dataSetUid);
    }

    @Provides
    @PerActivity
    FlowableProcessor<Unit> updateProcessor() {
        return PublishProcessor.create();
    }

}
