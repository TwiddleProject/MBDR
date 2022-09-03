package com.mbdr.modelbased.construction;

import java.util.*;
import java.util.Map.Entry;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.modelbased.structures.RankedInterpretation;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;

public class ModelRank implements RankConstructor<RankedInterpretation> {

    /**
     * Constructs the ranked model used to define Rational Closure for a given
     * knowledge base
     *
     * @param KB_C - Knowledge base containing all the purely classical formulas
     *             of
     *             the given knowledge base
     * @param KB_D - Knowledge base containing all the DIs of the given knowledge
     *             base
     * @return
     */
    @Override
    public RankedInterpretation construct(DefeasibleKnowledgeBase knowledge) {
        RankedInterpretation RankedModel = new RankedInterpretation(0);

        // Set of all possible worlds w.r.t. atoms
        Set<NicePossibleWorld> KB_U = NicePossibleWorld.getAllPossibleWorlds(
                knowledge.union().getMinimalSignature().toCollection());

        // Pre-emptively rank the worlds that don't satisfy the classical propositional
        // statements on the infinite rank
        Set<NicePossibleWorld> preliminaryInfiniteWorlds = new HashSet<>();
        for (NicePossibleWorld nw : KB_U) {
            if (!nw.satisfies(knowledge.getPropositionalKnowledge())) {
                preliminaryInfiniteWorlds.add(nw);
            }
        }

        // Remove the preliminary infinite worlds from all the possible worlds to rank
        Set<NicePossibleWorld> KB_U_remaining = new HashSet<>(KB_U);
        KB_U_remaining.removeAll(preliminaryInfiniteWorlds);

        // Remaining defeasible formulas to rank
        PlBeliefSet KB_D_remaining = knowledge.getDefeasibleKnowledge();

        // Mapping of defeasible formula antecedents to booleans to tick them off as
        // done (done when found a corresponding best world with finite rank)
        HashMap<PlFormula, Boolean> finished = new HashMap<>();
        // Initialise the entries in the hashmap
        for (PlFormula formula : KB_D_remaining) {
            PlFormula antecedent = ((Implication) formula).getFirstFormula();
            finished.put(antecedent, false);
        }

        Set<NicePossibleWorld> currentRank;

        do {

            currentRank = new HashSet<>();

            for (NicePossibleWorld nw : KB_U_remaining) {
                // if the world satisfies all the unticked off defeasible formulas
                if (nw.satisfies(KB_D_remaining)) {
                    currentRank.add(nw); // add the world to the current rank
                    // check whether the world is the best world for one of the formula antecedents
                    for (Entry<PlFormula, Boolean> entry : finished.entrySet()) {
                        if (!entry.getValue() && nw.satisfies(entry.getKey())) {
                            finished.replace(entry.getKey(), true);
                        }
                    }
                }
            }

            PlBeliefSet KB_D_remaining_previous = new PlBeliefSet(KB_D_remaining);
            // remove the formulas that have been checked off in the current rank
            for (PlFormula formula : KB_D_remaining_previous) {
                PlFormula antecedent = ((Implication) formula).getFirstFormula();
                if (finished.get(antecedent)) {
                    KB_D_remaining.remove(formula);
                }
            }

            if (!currentRank.isEmpty()) {
                int rankIndex = RankedModel.addRank();
                RankedModel.addToRank(rankIndex, currentRank);
                KB_U_remaining.removeAll(currentRank);
            }

        } while (!currentRank.isEmpty());

        // Add the infinite rank
        Set<NicePossibleWorld> infiniteRank = new HashSet<>();
        infiniteRank.addAll(KB_U_remaining);
        infiniteRank.addAll(preliminaryInfiniteWorlds);
        RankedModel.addToInfiniteRank(infiniteRank);

        return RankedModel;
    }

}
