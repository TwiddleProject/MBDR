package com.mbdr.modelbased.reasoning;

import com.mbdr.common.services.RankConstructor;
import com.mbdr.modelbased.structures.RankedFormulasInterpretation;

/**
 * This class provides a reasoner capable of answering defeasible entailment queries using the cumulative ranked formula
 * model produced by CumulativeFormulaRank.
 * Currently, this class is functionally equivalent to MinimalRankedEntailmentFormulaReasoner due to the semantics of
 * answering entailment queries with cumulative ranked models.
 */
public class MinimalRankedEntailmentCumulativeFormulaReasoner extends MinimalRankedEntailmentFormulaReasoner {

    /**
     * Constructor to produce reasoner with a pre-existing RankedFormulasInterpretation model
     * @param model
     */
    public MinimalRankedEntailmentCumulativeFormulaReasoner(RankedFormulasInterpretation model) {
        super(model);
    }

    /**
     * Constructor to produce reasoner with a RankConstructor capable of generating the necessary RankedFormulasInterpretation
     * model needed for reasoning
     * @param constructor - RankConstructor<RankedFormulasInterpretation>
     */
    public MinimalRankedEntailmentCumulativeFormulaReasoner(RankConstructor<RankedFormulasInterpretation> constructor) {
        super(constructor);
    }

}
