package org.dhis2.android.rtsm.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
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
public final class AppModule_ProvideDispatcherProviderFactory implements Factory<DispatcherProvider> {
  private final AppModule module;

  public AppModule_ProvideDispatcherProviderFactory(AppModule module) {
    this.module = module;
  }

  @Override
  public DispatcherProvider get() {
    return provideDispatcherProvider(module);
  }

  public static AppModule_ProvideDispatcherProviderFactory create(AppModule module) {
    return new AppModule_ProvideDispatcherProviderFactory(module);
  }

  public static DispatcherProvider provideDispatcherProvider(AppModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.provideDispatcherProvider());
  }
}
