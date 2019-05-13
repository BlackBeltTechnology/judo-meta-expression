package hu.blackbelt.judo.meta.expression.runtime.query;

import lombok.NonNull;

import java.util.List;

@lombok.Getter
@lombok.Builder
@lombok.ToString
public class SubSelect {

    /**
     * Joined data sets for subselect. {@link #getSelect()} is operating on latest item of the list.
     */
    @NonNull
    private List<Join> joins;

    /**
     * ASM entity type.
     */
    @NonNull
    private Select select;
}
