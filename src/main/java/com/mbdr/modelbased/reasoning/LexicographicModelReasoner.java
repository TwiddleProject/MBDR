package com.mbdr.modelbased.reasoning;

import com.mbdr.modelbased.construction.LexicographicRefineConstructor;

public class LexicographicModelReasoner extends MinimalRankedEntailmentReasoner{

    public LexicographicModelReasoner(){
        super(new LexicographicRefineConstructor());
    }

}
