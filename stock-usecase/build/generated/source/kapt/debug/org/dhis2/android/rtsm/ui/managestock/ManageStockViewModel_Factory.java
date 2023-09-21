package org.dhis2.android.rtsm.ui.managestock;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.reactivex.disposables.CompositeDisposable;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import org.dhis2.android.rtsm.services.SpeechRecognitionManager;
import org.dhis2.android.rtsm.services.StockManager;
import org.dhis2.android.rtsm.services.StockTableDimensionStore;
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper;
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.viewmodel.DispatcherProvider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class ManageStockViewModel_Factory implements Factory<ManageStockViewModel> {
  private final Provider<CompositeDisposable> disposableProvider;

  private final Provider<BaseSchedulerProvider> schedulerProvider;

  private final Provider<StockManager> stockManagerRepositoryProvider;

  private final Provider<RuleValidationHelper> ruleValidationHelperProvider;

  private final Provider<SpeechRecognitionManager> speechRecognitionManagerProvider;

  private final Provider<ResourceManager> resourcesProvider;

  private final Provider<TableModelMapper> tableModelMapperProvider;

  private final Provider<DispatcherProvider> dispatcherProvider;

  private final Provider<StockTableDimensionStore> tableDimensionStoreProvider;

  public ManageStockViewModel_Factory(Provider<CompositeDisposable> disposableProvider,
      Provider<BaseSchedulerProvider> schedulerProvider,
      Provider<StockManager> stockManagerRepositoryProvider,
      Provider<RuleValidationHelper> ruleValidationHelperProvider,
      Provider<SpeechRecognitionManager> speechRecognitionManagerProvider,
      Provider<ResourceManager> resourcesProvider,
      Provider<TableModelMapper> tableModelMapperProvider,
      Provider<DispatcherProvider> dispatcherProvider,
      Provider<StockTableDimensionStore> tableDimensionStoreProvider) {
    this.disposableProvider = disposableProvider;
    this.schedulerProvider = schedulerProvider;
    this.stockManagerRepositoryProvider = stockManagerRepositoryProvider;
    this.ruleValidationHelperProvider = ruleValidationHelperProvider;
    this.speechRecognitionManagerProvider = speechRecognitionManagerProvider;
    this.resourcesProvider = resourcesProvider;
    this.tableModelMapperProvider = tableModelMapperProvider;
    this.dispatcherProvider = dispatcherProvider;
    this.tableDimensionStoreProvider = tableDimensionStoreProvider;
  }

  @Override
  public ManageStockViewModel get() {
    return newInstance(disposableProvider.get(), schedulerProvider.get(), stockManagerRepositoryProvider.get(), ruleValidationHelperProvider.get(), speechRecognitionManagerProvider.get(), resourcesProvider.get(), tableModelMapperProvider.get(), dispatcherProvider.get(), tableDimensionStoreProvider.get());
  }

  public static ManageStockViewModel_Factory create(
      Provider<CompositeDisposable> disposableProvider,
      Provider<BaseSchedulerProvider> schedulerProvider,
      Provider<StockManager> stockManagerRepositoryProvider,
      Provider<RuleValidationHelper> ruleValidationHelperProvider,
      Provider<SpeechRecognitionManager> speechRecognitionManagerProvider,
      Provider<ResourceManager> resourcesProvider,
      Provider<TableModelMapper> tableModelMapperProvider,
      Provider<DispatcherProvider> dispatcherProvider,
      Provider<StockTableDimensionStore> tableDimensionStoreProvider) {
    return new ManageStockViewModel_Factory(disposableProvider, schedulerProvider, stockManagerRepositoryProvider, ruleValidationHelperProvider, speechRecognitionManagerProvider, resourcesProvider, tableModelMapperProvider, dispatcherProvider, tableDimensionStoreProvider);
  }

  public static ManageStockViewModel newInstance(CompositeDisposable disposable,
      BaseSchedulerProvider schedulerProvider, StockManager stockManagerRepository,
      RuleValidationHelper ruleValidationHelper, SpeechRecognitionManager speechRecognitionManager,
      ResourceManager resources, TableModelMapper tableModelMapper,
      DispatcherProvider dispatcherProvider, StockTableDimensionStore tableDimensionStore) {
    return new ManageStockViewModel(disposable, schedulerProvider, stockManagerRepository, ruleValidationHelper, speechRecognitionManager, resources, tableModelMapper, dispatcherProvider, tableDimensionStore);
  }
}
