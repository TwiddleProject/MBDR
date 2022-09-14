package com.mbdr.modelbased.reasoning;

import com.mbdr.modelbased.construction.ModelRank;

/**
 * Reasoner wrapper class used mainly for efficient benchmarking purposes.
 * Functions as a wrapper for the minimal ranked entailment reasoner that uses ranked models produced by ModelRank.
 */
public class RationalModelReasoner extends MinimalRankedEntailmentReasoner{
    
    public RationalModelReasoner(){
        super(new ModelRank());
    }

}
