package com.mbdr.modelbased.reasoning;

import com.mbdr.common.services.RankConstructor;
import com.mbdr.modelbased.structures.RankedFormulasInterpretation;

/**
 *
 */
public class MinimalRankedEntailmentCumulativeFormulaReasoner extends MinimalRankedEntailmentFormulaReasoner {

    /**
     *
     * @param model
     */
    public MinimalRankedEntailmentCumulativeFormulaReasoner(RankedFormulasInterpretation model) {
        super(model);
    }

    /**
     *
     * @param constructor
     */
    public MinimalRankedEntailmentCumulativeFormulaReasoner(RankConstructor<RankedFormulasInterpretation> constructor) {
        super(constructor);
    }

}
