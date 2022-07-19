package com.mbdr.modelbased.construction;

import java.util.*;
import java.util.Map.Entry;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;

public class RationalModelConstructor implements RankConstructor<ArrayList<Set<NicePossibleWorld>>>{

    /**
     * Constructs the ranked model used to define Rational Closure for a given
     * knowledge base
     * 
     * @param KB_C - Knowledge base containing all the purely classical formulas of
     *             the given knowledge base
     * @param KB_D - Knowledge base containing all the DIs of the given knowledge
     *             base
     * @return
     */
    @Override
    public ArrayList<Set<NicePossibleWorld>> construct(DefeasibleKnowledgeBase knowledge) {
        // TODO: Clean up more...
        // TODO: Add proper logger
        // TODO: Might want to change name of finite and infinite - maybe infinite and
        // potentially finite?
        ArrayList<Set<NicePossibleWorld>> RankedModel = new ArrayList<>();

        // System.out.println("------------------------------------------------------------------------------------");
        // System.out.println("Beginning construction of minimal ranked model for
        // RC...");
        // System.out.println("------------------------------------------------------------------------------------");
        Set<NicePossibleWorld> KB_U = NicePossibleWorld.getAllPossibleWorlds(
            knowledge.union().getMinimalSignature().toCollection()
        );

        // System.out.println("All Worlds:\t" + KB_U);
        // Find the worlds that do not satisfy the classical formulas - must have
        // infinite rank
        Set<NicePossibleWorld> KB_U_infinite = new HashSet<>();
        for (NicePossibleWorld nw : KB_U) {
            if (!nw.satisfies(knowledge.getPropositionalKnowledge())) {
                KB_U_infinite.add(nw);
            }
        }
        // System.out.println("Infinite Rank Worlds:\t" + KB_U_infinite);

        Set<NicePossibleWorld> KB_U_finite = new HashSet<>(KB_U);
        KB_U_finite.removeAll(KB_U_infinite);
        // System.out.println("Finite Rank Worlds:\t" + KB_U_finite);
        // System.out.println("------------------------------------------------------------------------------------");

        // Remaining defeasible formulas to rank
        PlBeliefSet KB_D_Current = knowledge.getDefeasibleKnowledge();
        // System.out.println("Defeasible formulas left to check off:\t" +
        // KB_D_Current);

        // Mapping of formula antecedents to booleans to tick them off as done (done
        // when found a corresponding best world with finite rank)
        HashMap<PlFormula, Boolean> finished = new HashMap<>();
        // Initialise the entries in the hashmap
        for (PlFormula formula : KB_D_Current) {
            PlFormula antecedent = ((Implication) formula).getFirstFormula();
            finished.put(antecedent, false);
        }

        // for (Entry<PlFormula, Boolean> entry : finished.entrySet()) {
        // System.out.println(entry);
        // }

        Set<NicePossibleWorld> KB_U_finite_current = new HashSet<>(KB_U_finite); // Current worlds without rank
        Set<NicePossibleWorld> Rank_Current = new HashSet<>();
        Set<NicePossibleWorld> Rank_Previous;

        // int rankIndex = 0;
        do {
            Rank_Previous = Rank_Current;
            Rank_Current = new HashSet<>();

            // for all the worlds without an assigned rank
            for (NicePossibleWorld nw : KB_U_finite_current) {
                // if the world satisfies all the unticked off defeasible formulas
                if (nw.satisfies(KB_D_Current)) {
                    Rank_Current.add(nw); // add the world to the current rank
                    // check whether the world is the best world for one of the formula antecedents
                    for (Entry<PlFormula, Boolean> entry : finished.entrySet()) {
                        if (!entry.getValue() && nw.satisfies(entry.getKey())) {
                            // System.out.println(nw + " satisfies " + entry.getKey());
                            finished.replace(entry.getKey(), true);
                        }
                    }
                }
            }

            // for (Entry<PlFormula, Boolean> entry : finished.entrySet()) {
            // System.out.println(entry);
            // }

            PlBeliefSet KB_D_Previous = new PlBeliefSet(KB_D_Current);
            // remove the formulas that have been checked off in the current rank
            for (PlFormula formula : KB_D_Previous) {
                // System.out.println(formula);
                PlFormula antecedent = ((Implication) formula).getFirstFormula();
                // System.out.println(antecedent);
                // System.out.println(finished.get(antecedent));
                if (finished.get(antecedent)) {
                    KB_D_Current.remove(formula);
                }
            }
            // System.out.println("Defeasible formulas left to check off:\t" +
            // KB_D_Current);

            KB_U_finite_current.removeAll(Rank_Current);

            // rankIndex++;

            // System.out.println("Rank " + rankIndex + ":\t" + Rank_Current);
            if (!Rank_Current.equals(Rank_Previous) && !Rank_Current.isEmpty()) {
                // System.out.println("Adding Rank_Current:\t" + Rank_Current);
                RankedModel.add(Rank_Current);
            }

        } while (!Rank_Current.isEmpty() || !Rank_Current.equals(Rank_Previous));

        // Add the infinite rank
        Set<NicePossibleWorld> infiniteRank = new HashSet<>();
        infiniteRank.addAll(Rank_Current);
        infiniteRank.addAll(KB_U_infinite);
        RankedModel.add(infiniteRank);

        return RankedModel;
    }

}
