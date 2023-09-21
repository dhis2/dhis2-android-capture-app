package org.dhis2.android.rtsm.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import org.hisp.dhis.android.core.D2;

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
public final class AppModule_ProvidesD2Factory implements Factory<D2> {
  private final AppModule module;

  public AppModule_ProvidesD2Factory(AppModule module) {
    this.module = module;
  }

  @Override
  public D2 get() {
    return providesD2(module);
  }

  public static AppModule_ProvidesD2Factory create(AppModule module) {
    return new AppModule_ProvidesD2Factory(module);
  }

  public static D2 providesD2(AppModule instance) {
    return Preconditions.checkNotNullFromProvides(instance.providesD2());
  }
}
