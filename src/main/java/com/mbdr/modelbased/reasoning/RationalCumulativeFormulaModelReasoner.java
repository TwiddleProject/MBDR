package com.mbdr.modelbased.reasoning;

import com.mbdr.modelbased.construction.CumulativeFormulaRank;

/**
 * Reasoner wrapper class used mainly for efficient benchmarking purposes.
 * Functions as a wrapper for the minimal ranked entailment formula reasoner that uses ranked formula models produced by
 * CumulativeFormulaRank.
 */
public class RationalCumulativeFormulaModelReasoner extends MinimalRankedEntailmentCumulativeFormulaReasoner {

    public RationalCumulativeFormulaModelReasoner() {
        super(new CumulativeFormulaRank());
    }
}
