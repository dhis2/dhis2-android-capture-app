package org.dhis2.android.rtsm.ui.base;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import org.dhis2.android.rtsm.services.SpeechRecognitionManager;
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
public final class SpeechRecognitionAwareViewModel_Factory implements Factory<SpeechRecognitionAwareViewModel> {
  private final Provider<BaseSchedulerProvider> schedulerProvider;

  private final Provider<SpeechRecognitionManager> speechRecognitionManagerProvider;

  public SpeechRecognitionAwareViewModel_Factory(Provider<BaseSchedulerProvider> schedulerProvider,
      Provider<SpeechRecognitionManager> speechRecognitionManagerProvider) {
    this.schedulerProvider = schedulerProvider;
    this.speechRecognitionManagerProvider = speechRecognitionManagerProvider;
  }

  @Override
  public SpeechRecognitionAwareViewModel get() {
    return newInstance(schedulerProvider.get(), speechRecognitionManagerProvider.get());
  }

  public static SpeechRecognitionAwareViewModel_Factory create(
      Provider<BaseSchedulerProvider> schedulerProvider,
      Provider<SpeechRecognitionManager> speechRecognitionManagerProvider) {
    return new SpeechRecognitionAwareViewModel_Factory(schedulerProvider, speechRecognitionManagerProvider);
  }

  public static SpeechRecognitionAwareViewModel newInstance(BaseSchedulerProvider schedulerProvider,
      SpeechRecognitionManager speechRecognitionManager) {
    return new SpeechRecognitionAwareViewModel(schedulerProvider, speechRecognitionManager);
  }
}
