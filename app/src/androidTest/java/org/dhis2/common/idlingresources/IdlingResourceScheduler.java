package org.dhis2.common.idlingresources;


import androidx.test.espresso.IdlingResource;
import io.reactivex.Scheduler;

//From:  https://github.com/square/RxIdler
/** A RxJava {@link Scheduler} that is also an Espresso {@link IdlingResource}. */
public abstract class IdlingResourceScheduler extends Scheduler implements IdlingResource {
}