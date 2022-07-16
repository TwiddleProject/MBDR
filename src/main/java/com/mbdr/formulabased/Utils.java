package com.mbdr.formulabased;

import java.util.List;

import org.tweetyproject.logics.pl.syntax.PlBeliefSet;

public class Utils {
    
    /**
     * Helper function written by Joel/Daniel to combine ranked PlBeliefSets into
     * single PlBeliefSet
     * 
     * @param ranks
     * @return
     */
    public static PlBeliefSet combine(List<PlBeliefSet> ranks) {
        PlBeliefSet combined = new PlBeliefSet();
        for (PlBeliefSet rank : ranks) {
            combined.addAll(rank);
        }
        return combined;
    }

    public static PlBeliefSet combine(PlBeliefSet[] ranks) {
        PlBeliefSet combined = new PlBeliefSet();
        for (PlBeliefSet rank : ranks) {
            combined.addAll(rank);
        }
        return combined;
    }

}
