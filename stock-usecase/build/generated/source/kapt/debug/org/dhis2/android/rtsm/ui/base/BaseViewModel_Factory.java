package org.dhis2.android.rtsm.ui.base;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider;

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
public final class BaseViewModel_Factory implements Factory<BaseViewModel> {
  private final Provider<BaseSchedulerProvider> schedulerProvider;

  public BaseViewModel_Factory(Provider<BaseSchedulerProvider> schedulerProvider) {
    this.schedulerProvider = schedulerProvider;
  }

  @Override
  public BaseViewModel get() {
    return newInstance(schedulerProvider.get());
  }

  public static BaseViewModel_Factory create(Provider<BaseSchedulerProvider> schedulerProvider) {
    return new BaseViewModel_Factory(schedulerProvider);
  }

  public static BaseViewModel newInstance(BaseSchedulerProvider schedulerProvider) {
    return new BaseViewModel(schedulerProvider);
  }
}
