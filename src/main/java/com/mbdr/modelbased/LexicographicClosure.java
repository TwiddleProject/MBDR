package com.mbdr.modelbased;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.structures.KnowledgeBase;

public class LexicographicClosure {
    
    private static int countSatisfied(NicePossibleWorld world, PlBeliefSet formulas){
        int count = 0;
        for(PlFormula formula : formulas){
            if(world.satisfies(formula)){
                ++count;
            }
        }
        return count;
    }

    /**
     * Binary insert to add world to list, sorted by formulas satisfied count
     * @param world The world to insert
     * @param array The list of counted worlds
     * @param start The start index (inclusive)
     * @param end The end index (inclusive)
     */
    private static void insert(CountedWorld world, ArrayList<CountedWorld> array, int start, int end){
        if(start==end){
            array.add(start, world);
        }
        else{
            int mid = (end+start)/2;
            if(array.get(mid).getCount()>=world.getCount()){
                insert(world, array, mid+1, end);
            }
            else{
                insert(world, array, start, mid);
            }
        }
    }

    private static void insert(CountedWorld world, ArrayList<CountedWorld> array){
        insert(world, array, 0, array.size());
    }

    /**
     * Refines rational closure model to produce lexicographic model
     * @param knowledge The knowledge base
     * @param rationalClosureModel The ranked model for rational closure
     * @return The model for lexicographic closure
     */
    public static ArrayList<Set<NicePossibleWorld>> refine(KnowledgeBase knowledge, ArrayList<Set<NicePossibleWorld>> rationalClosureModel){
        ArrayList<Set<NicePossibleWorld>> lexicographicClosureModel = new ArrayList<>();
        PlBeliefSet defeasibleKnowledge = knowledge.getDefeasibleKnowledge();
        // For each rank in the model for rational closure
        for(int index = 0; index < rationalClosureModel.size()-1; ++index){
            Set<NicePossibleWorld> currentRank = rationalClosureModel.get(index);
            ArrayList<CountedWorld> counts = new ArrayList<>(); 
            // Count formulas satisfied by each world, and add result to list (sorted by count)
            for(NicePossibleWorld world : currentRank){
                insert(new CountedWorld(world, countSatisfied(world, defeasibleKnowledge)), counts);
            }
            int currentCount = -1;
            // For each world
            for(CountedWorld world : counts){
                // If count different to previous, start a new rank in lexicographic closure model
                if(world.getCount() != currentCount){
                    currentCount = world.getCount();
                    Set<NicePossibleWorld> newRank = new HashSet<NicePossibleWorld>();
                    newRank.add(world.getWorld());
                    lexicographicClosureModel.add(newRank);
                }
                // Otherwise, add world to current rank
                else{
                    lexicographicClosureModel.get(lexicographicClosureModel.size()-1).add(world.getWorld());
                }
            }
        }
        // Add infinite rank
        lexicographicClosureModel.add(rationalClosureModel.get(rationalClosureModel.size()-1));
        return lexicographicClosureModel;
    }

    public static ArrayList<Set<NicePossibleWorld>> refine(KnowledgeBase knowledge){
        ArrayList<Set<NicePossibleWorld>> rationalClosureModel = RationalClosure.ConstructRankedModel(knowledge);
        return refine(knowledge, rationalClosureModel);
    }

    /**
     * Class to represent a world and the number of formulas it satisfies
     */
    public static class CountedWorld extends org.tweetyproject.commons.util.Pair<NicePossibleWorld, Integer>{

        public CountedWorld(NicePossibleWorld world, int count){
            super(world, count);
        }

        public NicePossibleWorld getWorld(){
            return getFirst();
        }

        public int getCount(){
            return getSecond();
        }
    }

}
