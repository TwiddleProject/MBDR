package com.mbdr.modelbased.reasoning;

import com.mbdr.modelbased.construction.LexicographicCountModelRank;

public class LexicographicModelReasoner extends MinimalRankedEntailmentReasoner{

    public LexicographicModelReasoner(){
        super(new LexicographicCountModelRank());
    }

}
