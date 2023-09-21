package org.dhis2.android.rtsm.services;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.reactivex.disposables.CompositeDisposable;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper;
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider;
import org.hisp.dhis.android.core.D2;

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
public final class StockManagerImpl_Factory implements Factory<StockManagerImpl> {
  private final Provider<D2> d2Provider;

  private final Provider<CompositeDisposable> disposableProvider;

  private final Provider<BaseSchedulerProvider> schedulerProvider;

  private final Provider<RuleValidationHelper> ruleValidationHelperProvider;

  public StockManagerImpl_Factory(Provider<D2> d2Provider,
      Provider<CompositeDisposable> disposableProvider,
      Provider<BaseSchedulerProvider> schedulerProvider,
      Provider<RuleValidationHelper> ruleValidationHelperProvider) {
    this.d2Provider = d2Provider;
    this.disposableProvider = disposableProvider;
    this.schedulerProvider = schedulerProvider;
    this.ruleValidationHelperProvider = ruleValidationHelperProvider;
  }

  @Override
  public StockManagerImpl get() {
    return newInstance(d2Provider.get(), disposableProvider.get(), schedulerProvider.get(), ruleValidationHelperProvider.get());
  }

  public static StockManagerImpl_Factory create(Provider<D2> d2Provider,
      Provider<CompositeDisposable> disposableProvider,
      Provider<BaseSchedulerProvider> schedulerProvider,
      Provider<RuleValidationHelper> ruleValidationHelperProvider) {
    return new StockManagerImpl_Factory(d2Provider, disposableProvider, schedulerProvider, ruleValidationHelperProvider);
  }

  public static StockManagerImpl newInstance(D2 d2, CompositeDisposable disposable,
      BaseSchedulerProvider schedulerProvider, RuleValidationHelper ruleValidationHelper) {
    return new StockManagerImpl(d2, disposable, schedulerProvider, ruleValidationHelper);
  }
}
