package com.mbdr.modelbased.reasoning;

import com.mbdr.modelbased.construction.RationalModelConstructor;

public class RationalModelReasoner extends MinimalRankedEntailmentReasoner{
    
    public RationalModelReasoner(){
        super(new RationalModelConstructor());
    }

}
