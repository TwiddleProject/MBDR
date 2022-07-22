package com.mbdr.modelbased.construction;

import java.util.*;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.formulabased.construction.BaseRankConstructor;
import com.mbdr.modelbased.structures.RankedInterpretation;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;

//TODO Refactor to be RankConstructor<RankedInterpretation>
public class RationalModelBaseRankConstructor implements RankConstructor<RankedInterpretation>{


    /**
     * Constructs the ranked model used to define Rational Closure for a given
     * knowledge base using BaseRank to facilitate its construction
     * 
     * @param knowledge
     * @return
     */
    @Override
    public RankedInterpretation construct(DefeasibleKnowledgeBase knowledge) {
        
        // Apply BaseRank to the knowledge base
        ArrayList<PlBeliefSet> ranked_KB = new BaseRankConstructor().construct(knowledge);

        // System.out.println("ranked_KB:\t" + ranked_KB);

        // HashMap of finite knowledge base formulas to their base rank
        HashMap<PlFormula, Integer> kB_BR_HashMap = new HashMap<>();

        for (int i = 0; i < ranked_KB.size() - 1; i++) {
            for (PlFormula formula : ranked_KB.get(i)) {
                kB_BR_HashMap.put(formula, i);
            }
        }
        // System.out.println("kB_BR_HashMap:\t" + kB_BR_HashMap);

        // Set to contain all the infinite rank worlds
        Set<NicePossibleWorld> infinite_worlds = new HashSet<>();

        ArrayList<Set<NicePossibleWorld>> ranked_model = new ArrayList<>();

        // Initialise the ranked model to have the minimum number of ranks - i.e. as
        // many ranks as there are base ranks
        for (int i = 0; i < ranked_KB.size(); i++) {
            ranked_model.add(new HashSet<NicePossibleWorld>());
        }

        Set<NicePossibleWorld> kB_PossibleWorlds = NicePossibleWorld.getAllPossibleWorlds(
            knowledge.union().getMinimalSignature().toCollection()
        );

        for (NicePossibleWorld pWorld : kB_PossibleWorlds) {

            // If the current world does not satisfy the classical propositional formulas
            // then add to infinite rank
            if (!pWorld.satisfies(knowledge.getPropositionalKnowledge())) {
                infinite_worlds.add(pWorld);
            } else {
                int tempMaxRank = 0;

                // Find the maximum rank of a knowledge base formula for which the current
                // world satisfies its antecedent
                for (PlFormula formula : knowledge.getDefeasibleKnowledge()) {
                    PlFormula antecedent = ((Implication) formula).getFirstFormula();
                    if (pWorld.satisfies(antecedent) && kB_BR_HashMap.get(formula) >= tempMaxRank) {
                        tempMaxRank = kB_BR_HashMap.get(formula);
                    }
                }

                // If the current world does not satisfy all the formulas on its max rank then
                // bump it up
                if (!pWorld.satisfies(ranked_KB.get(tempMaxRank))) {
                    tempMaxRank++;
                }

                // If the rank of the world exceeds the number of ranks that exist, then create
                // a new rank, add the current world to it and add the rank to the ranked model
                if (tempMaxRank >= ranked_model.size()) {
                    Set<NicePossibleWorld> newRank = new HashSet<>();
                    newRank.add(pWorld);
                    ranked_model.add(tempMaxRank, newRank); // Add rank at correct rank index
                } else {
                    // Otherwise add the world to its correct rank in the ranked model
                    Set<NicePossibleWorld> rank = ranked_model.get(tempMaxRank);
                    rank.add(pWorld);
                }
            }
        }

        // Lastly add the infinite rank worlds
        ranked_model.add(infinite_worlds);

        return new RankedInterpretation(ranked_model);

    }

}
