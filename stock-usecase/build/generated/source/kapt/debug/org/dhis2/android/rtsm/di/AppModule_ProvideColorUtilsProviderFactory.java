package org.dhis2.android.rtsm.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import org.dhis2.commons.resources.ColorUtils;

@ScopeMetadata("javax.inject.Singleton")
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
public final class AppModule_ProvideColorUtilsProviderFactory implements Factory<ColorUtils> {
  private final AppModule module;

  public AppModule_ProvideColorUtilsProviderFactory(AppModule module) {
    this.module = module;
  }

  @Override
  public ColorUtils get() {
    return provideColorUtilsProvider(module);
  }

  public static AppModule_ProvideColorUtilsProviderFactory create(AppModule module) {
    return new AppModule_ProvideColorUtilsProviderFactory(module);
  }

  public static ColorUtils provideColorUtilsProvider(AppModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.provideColorUtilsProvider());
  }
}
