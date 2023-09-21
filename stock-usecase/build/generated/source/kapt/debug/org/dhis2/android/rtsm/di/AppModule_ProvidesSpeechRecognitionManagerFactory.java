package org.dhis2.android.rtsm.di;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import org.dhis2.android.rtsm.services.SpeechRecognitionManager;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppModule_ProvidesSpeechRecognitionManagerFactory implements Factory<SpeechRecognitionManager> {
  private final AppModule module;

  private final Provider<Context> appContextProvider;

  public AppModule_ProvidesSpeechRecognitionManagerFactory(AppModule module,
      Provider<Context> appContextProvider) {
    this.module = module;
    this.appContextProvider = appContextProvider;
  }

  @Override
  public SpeechRecognitionManager get() {
    return providesSpeechRecognitionManager(module, appContextProvider.get());
  }

  public static AppModule_ProvidesSpeechRecognitionManagerFactory create(AppModule module,
      Provider<Context> appContextProvider) {
    return new AppModule_ProvidesSpeechRecognitionManagerFactory(module, appContextProvider);
  }

  public static SpeechRecognitionManager providesSpeechRecognitionManager(AppModule instance,
      Context appContext) {
    return Preconditions.checkNotNullFromProvides(instance.providesSpeechRecognitionManager(appContext));
  }
}
