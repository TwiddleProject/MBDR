package com.mbdr.modelbased.reasoning;

import com.mbdr.modelbased.construction.LexicographicCountCumulativeFormulaRank;

/**
 * Reasoner for count-based lexicographic closure, using
 * Cumulative formula representation
 */
public class LexicographicCumulativeFormulaReasoner extends MinimalRankedEntailmentFormulaReasoner{

    /**
     * Default constructor
     */
    public LexicographicCumulativeFormulaReasoner(){
        super(new LexicographicCountCumulativeFormulaRank());
    }

}
