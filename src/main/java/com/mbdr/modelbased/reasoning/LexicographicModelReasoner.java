package com.mbdr.modelbased.reasoning;

import com.mbdr.modelbased.construction.LexicographicCountModelRank;

/**
 * Reasoner for count-based lexicographic closure, using
 * the ranked model representation
 */
public class LexicographicModelReasoner extends MinimalRankedEntailmentReasoner{

    /**
     * Default constructor
     */
    public LexicographicModelReasoner(){
        super(new LexicographicCountModelRank());
    }

}
