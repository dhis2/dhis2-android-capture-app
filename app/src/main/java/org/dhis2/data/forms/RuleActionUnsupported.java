package org.dhis2.data.forms;

import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.rules.models.RuleAction;

import javax.annotation.Nonnull;

/**
 * QUADRAM. Created by ppajuelo on 28/03/2019.
 */
@AutoValue
public abstract class RuleActionUnsupported extends RuleAction {

    /**
     * @return a message to show to user
     * when an actionType is not supported
     */
    @Nonnull
    public abstract String content();

    /**
     * @return name of the unsupported action.
     */
    @Nonnull
    public abstract String actionValueType();

    @Nonnull
    public static RuleActionUnsupported create(
            @NonNull String content, @Nonnull String actionValueType) {
        return new AutoValue_RuleActionUnsupported("", content, actionValueType);
    }
}
