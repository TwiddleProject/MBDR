package com.mbdr.modelbased;

import java.util.*;
import java.util.Map.Entry;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.syntax.PlSignature;

import com.mbdr.structures.DefeasibleKnowledgeBase;
import com.mbdr.utils.parsing.Parser;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.commons.util.Pair;
import org.tweetyproject.logics.pl.reasoner.*;

public class RationalClosure {

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
    public static ArrayList<Set<NicePossibleWorld>> ConstructRankedModel(DefeasibleKnowledgeBase knowledge,
            Set<NicePossibleWorld> possibleWorlds) {
        // TODO: Clean up more...
        // TODO: Add proper logger
        // TODO: Might want to change name of finite and infinite - maybe infinite and
        // potentially finite?
        ArrayList<Set<NicePossibleWorld>> RankedModel = new ArrayList<>();

        // System.out.println("------------------------------------------------------------------------------------");
        // System.out.println("Beginning construction of minimal ranked model for
        // RC...");
        // System.out.println("------------------------------------------------------------------------------------");
        PlBeliefSet KB = knowledge.union();
        PlSignature KB_atoms = KB.getMinimalSignature();
        // System.out.println("Atoms:\t" + KB_atoms);

        Set<NicePossibleWorld> KB_U;
        if (possibleWorlds == null) {
            KB_U = NicePossibleWorld.getAllPossibleWorlds(KB_atoms.toCollection());
        } else {
            KB_U = new HashSet<>(possibleWorlds);
        }

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

    /**
     * Constructs the ranked model used to define Rational Closure for a given
     * knowledge base using BaseRank to facilitate its construction
     * 
     * @param knowledge
     * @param possibleWorlds
     * @return
     */
    public static ArrayList<Set<NicePossibleWorld>> ConstructRankedModelBaseRank(DefeasibleKnowledgeBase knowledge,
            Set<NicePossibleWorld> possibleWorlds) {

        // Apply BaseRank to the knowledge base
        ArrayList<PlBeliefSet> ranked_KB = com.mbdr.formulabased.BaseRank.BaseRankDirectImplementation(knowledge);

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

        for (NicePossibleWorld pWorld : possibleWorlds) {

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

        return ranked_model;

    }

}
