package com.mbdr.modelbased.reasoning;

import com.mbdr.modelbased.construction.LexicographicCountFormulaRank;

/**
 * Reasoner for count-based lexicographic closure, using
 * formula representation
 */
public class LexicographicFormulaReasoner extends MinimalRankedEntailmentFormulaReasoner{

    /**
     * Default constructor
     */
    public LexicographicFormulaReasoner(){
        super(new LexicographicCountFormulaRank());
    }

}
