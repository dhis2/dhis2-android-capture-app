package org.dhis2.data.dagger

import java.lang.annotation.RetentionPolicy

import javax.inject.Scope
import kotlin.annotation.Retention

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerActivity
