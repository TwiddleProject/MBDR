package com.mbdr.modelbased.reasoning;

import com.mbdr.common.services.RankConstructor;
import com.mbdr.modelbased.structures.RankedFormulasInterpretation;

public class MinimalRankedEntailmentCumulativeFormulaReasoner extends MinimalRankedEntailmentFormulaReasoner {

    public MinimalRankedEntailmentCumulativeFormulaReasoner(RankedFormulasInterpretation model) {
        super(model);
    }

    public MinimalRankedEntailmentCumulativeFormulaReasoner(RankConstructor<RankedFormulasInterpretation> constructor) {
        super(constructor);
    }

}
