package com.mbdr.modelbased;

import java.util.*;
import java.util.Map.Entry;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.syntax.PlSignature;

import com.mbdr.structures.KnowledgeBase;
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
    public static ArrayList<Set<NicePossibleWorld>> ConstructRankedModel(KnowledgeBase knowledge,
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
    public static ArrayList<Set<NicePossibleWorld>> ConstructRankedModelBaseRank(KnowledgeBase knowledge,
            Set<NicePossibleWorld> possibleWorlds) {

        // PlBeliefSet kBunion = knowledge.union();
        // System.out.println("knowledgeBase union:\t" + kBunion);
        // PlSignature kBsignature = kBunion.getMinimalSignature();
        // System.out.println("knowledgeBase signature:\t" + kBsignature);

        // Set<NicePossibleWorld> kB_PossibleWorlds = NicePossibleWorld
        // .getAllPossibleWorlds(kBsignature.toCollection());
        // System.out.println("kB_PossibleWorlds:\t" + kB_PossibleWorlds);

        Set<NicePossibleWorld> infinite_worlds = new HashSet<>();

        for (NicePossibleWorld pWorld : possibleWorlds) {
            if (!pWorld.satisfies(knowledge.getPropositionalKnowledge())) {
                infinite_worlds.add(pWorld);
            }
        }

        Set<NicePossibleWorld> finite_worlds = new HashSet<>(possibleWorlds);
        finite_worlds.removeAll(infinite_worlds);

        // System.out.println("infinite_worlds:\t" + infinite_worlds);
        // System.out.println("finite_worlds:\t\t" + finite_worlds);

        // Now we need to rank the finite worlds using the base rank information hence
        // we
        // apply BaseRank to the knowledge base
        ArrayList<PlBeliefSet> ranked_KB = com.mbdr.formulabased.BaseRank.BaseRankDirectImplementation(knowledge);

        // HashMap of finite knowledge base formulas to their base rank
        HashMap<PlFormula, Integer> kB_BR_HashMap = new HashMap<>();

        for (int i = 0; i < ranked_KB.size() - 1; i++) {
            for (PlFormula formula : ranked_KB.get(i)) {
                kB_BR_HashMap.put(formula, i);
            }
        }
        // System.out.println(kB_BR_HashMap);

        // Map every finite rank world to the formula with the max base rank whose
        // antecedent they satisfy
        HashMap<NicePossibleWorld, Pair<PlFormula, Integer>> world_max_rank_map = new HashMap<>();

        for (NicePossibleWorld finiteWorld : finite_worlds) {
            // System.out.println("Current finite world:\t" + finiteWorld);
            world_max_rank_map.put(finiteWorld, null);
            int tempMaxRank = 0;
            PlFormula tempMaxFormula = null;
            for (PlFormula formula : knowledge.getDefeasibleKnowledge()) {
                PlFormula antecedent = ((Implication) formula).getFirstFormula();
                if (finiteWorld.satisfies(antecedent) && kB_BR_HashMap.get(formula) >= tempMaxRank) {
                    tempMaxRank = kB_BR_HashMap.get(formula);
                    tempMaxFormula = formula;
                }
            }

            world_max_rank_map.put(finiteWorld, new Pair<PlFormula, Integer>(tempMaxFormula, tempMaxRank));

        }

        ArrayList<Set<NicePossibleWorld>> preliminary_ranked_model = new ArrayList<>();

        // Initialise the ranked model to have the minimum number of ranks - i.e. as
        // many ranks as there are base ranks
        for (int i = 0; i < ranked_KB.size(); i++) {
            preliminary_ranked_model.add(new HashSet<NicePossibleWorld>());
        }

        world_max_rank_map.entrySet().forEach(entry -> {

            // Store the max base rank value of the current entry
            int rankIndex = entry.getValue().getSecond();
            // System.out.println("rankIndex:\t" + rankIndex);

            Set<NicePossibleWorld> rank = preliminary_ranked_model.get(rankIndex);
            rank.add(entry.getKey());

        });

        // System.out.println("----------------------------");
        // System.out.println("Preliminary Ranked Model:");
        // System.out.println("----------------------------");

        // System.out.println(preliminary_ranked_model);

        // System.out.println("----------------------------");

        // System.out.println("∞" + " :\t" + infinite_worlds);
        // for (int rank_Index = preliminary_ranked_model.size() - 1; rank_Index >= 0;
        // rank_Index--) {
        // System.out.println(rank_Index + " :\t" +
        // preliminary_ranked_model.get(rank_Index));
        // }

        for (int rankIndex = 0; rankIndex < preliminary_ranked_model.size(); rankIndex++) {
            for (NicePossibleWorld world : preliminary_ranked_model.get(rankIndex)) {
                if (!world.satisfies(ranked_KB.get(rankIndex))) {
                    int previousIndex = world_max_rank_map.get(world).getSecond();
                    world_max_rank_map.get(world).setSecond(previousIndex + 1);
                }
            }
        }

        ArrayList<Set<NicePossibleWorld>> updated_ranked_model = new ArrayList<>();

        // Initialise the ranked model to have the minimum number of ranks - i.e. as
        // many ranks as there are base ranks
        for (int i = 0; i < ranked_KB.size(); i++) {
            updated_ranked_model.add(new HashSet<NicePossibleWorld>());
        }

        world_max_rank_map.entrySet().forEach(entry -> {

            // Store the max base rank value of the current entry
            int rankIndex = entry.getValue().getSecond();

            if (rankIndex >= updated_ranked_model.size()) {
                Set<NicePossibleWorld> newRank = new HashSet<>();
                newRank.add(entry.getKey());
                updated_ranked_model.add(rankIndex, newRank); // Add rank at correct rank index

            } else {
                Set<NicePossibleWorld> rank = updated_ranked_model.get(rankIndex);
                rank.add(entry.getKey());
            }

        });

        // System.out.println("----------------------------");
        // System.out.println("Updated Ranked Model:");
        // System.out.println("----------------------------");

        // System.out.println(updated_ranked_model);

        // System.out.println("----------------------------");

        // System.out.println("∞" + " :\t" + infinite_worlds);
        // for (int rank_Index = updated_ranked_model.size() - 1; rank_Index >= 0;
        // rank_Index--) {
        // System.out.println(rank_Index + " :\t" +
        // updated_ranked_model.get(rank_Index));
        // }

        updated_ranked_model.add(infinite_worlds);

        return updated_ranked_model;

    }

}
