package org.dhis2.usescases.enrollment;

import org.dhis2.data.dagger.PerActivity;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = EnrollmentModule.class)
public interface EnrollmentComponent {
    void inject(EnrollmentActivity activity);
}
