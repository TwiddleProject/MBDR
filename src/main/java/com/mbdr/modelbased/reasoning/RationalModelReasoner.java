package com.mbdr.modelbased.reasoning;

import com.mbdr.modelbased.construction.ModelRank;

public class RationalModelReasoner extends MinimalRankedEntailmentReasoner{
    
    public RationalModelReasoner(){
        super(new ModelRank());
    }

}
