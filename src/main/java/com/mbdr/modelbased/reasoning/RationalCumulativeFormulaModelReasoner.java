package com.mbdr.modelbased.reasoning;

import com.mbdr.modelbased.construction.CumulativeFormulaRank;

public class RationalCumulativeFormulaModelReasoner extends MinimalRankedEntailmentCumulativeFormulaReasoner {

    public RationalCumulativeFormulaModelReasoner() {
        super(new CumulativeFormulaRank());
    }
}
