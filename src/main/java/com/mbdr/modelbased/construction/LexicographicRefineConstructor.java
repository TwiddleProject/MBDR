package com.mbdr.modelbased.construction;

import java.util.ArrayList;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.modelbased.structures.RankedInterpretation;

public class LexicographicRefineConstructor implements RankConstructor<RankedInterpretation>{

    private RankedInterpretation rationalClosureModel;

    public LexicographicRefineConstructor(){
        this.rationalClosureModel = null;
    }

    public LexicographicRefineConstructor(RankedInterpretation rationalClosureModel){
        this.rationalClosureModel = rationalClosureModel;
    }

    public void setRationalClosureModel(RankedInterpretation rationalClosureModel){
        this.rationalClosureModel = rationalClosureModel;
    }

    private int countSatisfied(NicePossibleWorld world, PlBeliefSet formulas) {
        int count = 0;
        for (PlFormula formula : formulas) {
            if (world.satisfies(formula)) {
                ++count;
            }
        }
        return count;
    }

    /**
     * Binary insert to add world to list, sorted by formulas satisfied count
     * 
     * @param world The world to insert
     * @param array The list of counted worlds
     * @param start The start index (inclusive)
     * @param end   The end index (inclusive)
     */
    private void insert(CountedWorld world, ArrayList<CountedWorld> array, int start, int end) {
        if (start == end) {
            array.add(start, world);
        } else {
            int mid = (end + start) / 2;
            if (array.get(mid).getCount() >= world.getCount()) {
                insert(world, array, mid + 1, end);
            } else {
                insert(world, array, start, mid);
            }
        }
    }

    private void insert(CountedWorld world, ArrayList<CountedWorld> array) {
        insert(world, array, 0, array.size());
    }

    /**
     * Refines rational closure model to produce lexicographic model
     * 
     * @param knowledge            The knowledge base
     * @return The model for lexicographic closure
     */
    public RankedInterpretation construct(DefeasibleKnowledgeBase knowledge) {
        if(this.rationalClosureModel == null){
            this.rationalClosureModel = new RankedInterpretation(
                new RationalModelBaseRankConstructor().construct(knowledge)
            );
        }
        RankedInterpretation lexicographicClosureModel = new RankedInterpretation(0);
        PlBeliefSet defeasibleKnowledge = knowledge.getDefeasibleKnowledge();
        // For each rank in the model for rational closure
        for (int index = 0; index < this.rationalClosureModel.getRankCount(); ++index) {
            ArrayList<CountedWorld> counts = new ArrayList<>();
            // Count formulas satisfied by each world, and add result to list (sorted by
            // count)
            for (NicePossibleWorld world : this.rationalClosureModel.getRank(index)) {
                insert(new CountedWorld(world, countSatisfied(world, defeasibleKnowledge)), counts);
            }
            int currentCount = -1;
            // For each world
            for (CountedWorld world : counts) {
                // If count different to previous, start a new rank in lexicographic closure
                // model
                if (world.getCount() != currentCount) {
                    currentCount = world.getCount();
                    lexicographicClosureModel.addRank();
                    lexicographicClosureModel.addToRank(world.getWorld());
                }
                // Otherwise, add world to current rank
                else {
                    lexicographicClosureModel.addToRank(world.getWorld());
                }
            }
        }
        // Add infinite rank
        lexicographicClosureModel.addToInfiniteRank(this.rationalClosureModel.getInfiniteRank());
        return lexicographicClosureModel;
    }

    /**
     * Class to represent a world and the number of formulas it satisfies
     */
    private static class CountedWorld extends org.tweetyproject.commons.util.Pair<NicePossibleWorld, Integer> {

        public CountedWorld(NicePossibleWorld world, int count) {
            super(world, count);
        }

        public NicePossibleWorld getWorld() {
            return getFirst();
        }

        public int getCount() {
            return getSecond();
        }
    }

}
