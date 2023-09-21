package org.dhis2.android.rtsm.services.scheduler;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
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
public final class SchedulerProviderImpl_Factory implements Factory<SchedulerProviderImpl> {
  @Override
  public SchedulerProviderImpl get() {
    return newInstance();
  }

  public static SchedulerProviderImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SchedulerProviderImpl newInstance() {
    return new SchedulerProviderImpl();
  }

  private static final class InstanceHolder {
    private static final SchedulerProviderImpl_Factory INSTANCE = new SchedulerProviderImpl_Factory();
  }
}
