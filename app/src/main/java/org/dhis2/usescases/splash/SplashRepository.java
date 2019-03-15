package org.dhis2.usescases.splash;

import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 16/05/2018.
 */

public interface SplashRepository {
    Observable<String> getIconForFlag();
}
