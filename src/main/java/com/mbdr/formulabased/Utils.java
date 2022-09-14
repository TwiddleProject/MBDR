package com.mbdr.formulabased;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.syntax.PlSignature;

/**
 * Formula utilities used in formula-based reasoning algorithms
 */
public class Utils {
    
    /**
     * Helper function written by Joel/Daniel to combine ranked PlBeliefSets into
     * single PlBeliefSet
     * 
     * @param ranks The ranks to combine
     * @return The combined ranks
     */
    public static PlBeliefSet combine(List<PlBeliefSet> ranks) {
        PlBeliefSet combined = new PlBeliefSet();
        for (PlBeliefSet rank : ranks) {
            combined.addAll(rank);
        }
        return combined;
    }

    /**
     * Helper function written by Joel/Daniel to combine ranked PlBeliefSets into
     * single PlBeliefSet
     * 
     * @param ranks The ranks to combine
     * @return The combined ranks
     */
    public static PlBeliefSet combine(PlBeliefSet[] ranks) {
        PlBeliefSet combined = new PlBeliefSet();
        for (PlBeliefSet rank : ranks) {
            combined.addAll(rank);
        }
        return combined;
    }

    /**
     * Generates models of a formula
     * 
     * @param formula The formula
     * @param signature The atom signature
     * @return The models
     */
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
