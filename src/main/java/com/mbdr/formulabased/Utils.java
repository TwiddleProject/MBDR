package com.mbdr.formulabased;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.syntax.PlSignature;

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

    public static Set<NicePossibleWorld> getModels(PlFormula formula, PlSignature signature){
        HashSet<NicePossibleWorld> result = new HashSet<>();
        for(NicePossibleWorld world: NicePossibleWorld.getAllPossibleWorlds(signature.toCollection())){
            if(world.satisfies(formula)){
                result.add(world);
            }
        }
        return result;
    }

}
