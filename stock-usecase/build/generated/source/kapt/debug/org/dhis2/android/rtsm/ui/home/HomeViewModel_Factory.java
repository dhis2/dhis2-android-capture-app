package org.dhis2.android.rtsm.ui.home;

import androidx.lifecycle.SavedStateHandle;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.reactivex.disposables.CompositeDisposable;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import org.dhis2.android.rtsm.services.MetadataManager;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<CompositeDisposable> disposableProvider;

  private final Provider<BaseSchedulerProvider> schedulerProvider;

  private final Provider<MetadataManager> metadataManagerProvider;

  private final Provider<SavedStateHandle> savedStateProvider;

  public HomeViewModel_Factory(Provider<CompositeDisposable> disposableProvider,
      Provider<BaseSchedulerProvider> schedulerProvider,
      Provider<MetadataManager> metadataManagerProvider,
      Provider<SavedStateHandle> savedStateProvider) {
    this.disposableProvider = disposableProvider;
    this.schedulerProvider = schedulerProvider;
    this.metadataManagerProvider = metadataManagerProvider;
    this.savedStateProvider = savedStateProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(disposableProvider.get(), schedulerProvider.get(), metadataManagerProvider.get(), savedStateProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<CompositeDisposable> disposableProvider,
      Provider<BaseSchedulerProvider> schedulerProvider,
      Provider<MetadataManager> metadataManagerProvider,
      Provider<SavedStateHandle> savedStateProvider) {
    return new HomeViewModel_Factory(disposableProvider, schedulerProvider, metadataManagerProvider, savedStateProvider);
  }

  public static HomeViewModel newInstance(CompositeDisposable disposable,
      BaseSchedulerProvider schedulerProvider, MetadataManager metadataManager,
      SavedStateHandle savedState) {
    return new HomeViewModel(disposable, schedulerProvider, metadataManager, savedState);
  }
}
