package org.dhis2.android.rtsm.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.reactivex.disposables.CompositeDisposable;
import javax.annotation.processing.Generated;

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
public final class SystemModule_ProvidesDisposableFactory implements Factory<CompositeDisposable> {
  @Override
  public CompositeDisposable get() {
    return providesDisposable();
  }

  public static SystemModule_ProvidesDisposableFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static CompositeDisposable providesDisposable() {
    return Preconditions.checkNotNullFromProvides(SystemModule.INSTANCE.providesDisposable());
  }

  private static final class InstanceHolder {
    private static final SystemModule_ProvidesDisposableFactory INSTANCE = new SystemModule_ProvidesDisposableFactory();
  }
}
