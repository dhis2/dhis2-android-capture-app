package org.dhis2.android.rtsm.di;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.commons.resources.ResourceManager;

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
public final class AppModule_ProvideResourcesProviderFactory implements Factory<ResourceManager> {
  private final AppModule module;

  private final Provider<Context> appContextProvider;

  private final Provider<ColorUtils> colorUtilsProvider;

  public AppModule_ProvideResourcesProviderFactory(AppModule module,
      Provider<Context> appContextProvider, Provider<ColorUtils> colorUtilsProvider) {
    this.module = module;
    this.appContextProvider = appContextProvider;
    this.colorUtilsProvider = colorUtilsProvider;
  }

  @Override
  public ResourceManager get() {
    return provideResourcesProvider(module, appContextProvider.get(), colorUtilsProvider.get());
  }

  public static AppModule_ProvideResourcesProviderFactory create(AppModule module,
      Provider<Context> appContextProvider, Provider<ColorUtils> colorUtilsProvider) {
    return new AppModule_ProvideResourcesProviderFactory(module, appContextProvider, colorUtilsProvider);
  }

  public static ResourceManager provideResourcesProvider(AppModule instance, Context appContext,
      ColorUtils colorUtils) {
    return Preconditions.checkNotNullFromProvides(instance.provideResourcesProvider(appContext, colorUtils));
  }
}
