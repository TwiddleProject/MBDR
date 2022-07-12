package com.mbdr.formulabased;

import java.io.IOException;
import java.util.*;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.structures.DefeasibleKnowledgeBase;
import com.mbdr.utils.parsing.Parser;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.reasoner.*;

public class RationalClosure {

    // (For use with Joel's indexing algorithms) Used to store the rank at which a
    // given query is no longer exceptional with the knowledge base
    HashMap<PlFormula, Integer> antecedentNegationRanksToRemoveFrom = new HashMap<PlFormula, Integer>();

    /**
     * Standard, unoptimised RationalClosure algorithm implementation
     * 
     * @param KB_C     - Knowledge base containing all the purely classical formulas
     *                 of the given knowledge base
     * @param KB_D     - Knowledge base containing all the DIs of the given
     *                 knowledge base
     * @param query_DI - query to check
     * @return
     */
    public static boolean RationalClosureDirectImplementation(DefeasibleKnowledgeBase knowledge, Implication query_DI) {
        SatSolver.setDefaultSolver(new Sat4jSolver());
        SatReasoner reasoner = new SatReasoner();

        ArrayList<PlBeliefSet> ranked_KB = new BaseRankConstructor().construct(knowledge);
        PlBeliefSet R = knowledge.getDefeasibleKnowledge();
        Negation query_negated_antecedent = new Negation(query_DI.getFirstFormula());

        int i = 0;
        while (reasoner.query(DefeasibleKnowledgeBase.union(knowledge.getPropositionalKnowledge(), R), query_negated_antecedent)
                && !R.isEmpty()) {
            R.removeAll(ranked_KB.get(i));
            i++;
        }

        return reasoner.query(DefeasibleKnowledgeBase.union(knowledge.getPropositionalKnowledge(), R), query_DI);
    }

    public static boolean RationalClosureDirectImplementation_Benchmarking(ArrayList<PlBeliefSet> ranked_KB,
            DefeasibleKnowledgeBase knowledge,
            String rawQuery) throws ParserException, IOException {

        PlParser parser = new PlParser();

        Implication query_DI = (Implication) parser
                .parseFormula(Parser.materialiseDefeasibleImplication(rawQuery));

        SatSolver.setDefaultSolver(new Sat4jSolver());
        SatReasoner reasoner = new SatReasoner();

        PlBeliefSet R = knowledge.getDefeasibleKnowledge();
        Negation query_negated_antecedent = new Negation(query_DI.getFirstFormula());

        int i = 0;
        while (reasoner.query(DefeasibleKnowledgeBase.union(knowledge.getPropositionalKnowledge(), R), query_negated_antecedent)
                && !R.isEmpty()) {
            R.removeAll(ranked_KB.get(i));
            i++;
        }

        return reasoner.query(DefeasibleKnowledgeBase.union(knowledge.getPropositionalKnowledge(), R), query_DI);
    }

    /**
     * Joel's implementation of standard RationalClosure algorithm
     * 
     * @param originalRankedKB - ranked knowledge base output from BaseRank
     *                         algorithm
     * @param rawQuery         - defeasible query string to check
     * @return
     * @throws IOException
     * @throws ParserException
     */
    public static boolean RationalClosureJoelRegular(ArrayList<PlBeliefSet> originalRankedKB, String rawQuery)
            throws ParserException, IOException {

        PlFormula formula = Parser.parseDefeasibleFormula(rawQuery);

        SatSolver.setDefaultSolver(new Sat4jSolver());
        SatReasoner classicalReasoner = new SatReasoner();
        PlFormula negationOfAntecedent = new Negation(((Implication) formula).getFormulas().getFirst());
        ArrayList<PlBeliefSet> rankedKB = (ArrayList<PlBeliefSet>) originalRankedKB.clone();
        PlBeliefSet combinedRankedKB = combine(rankedKB);
        while (combinedRankedKB.size() != 0) {
            // System.out.println("We are checking whether or not " +
            // negationOfAntecedent.toString() + " is entailed by: " +
            // combinedRankedKB.toString());
            if (classicalReasoner.query(combinedRankedKB, negationOfAntecedent)) {
                // System.out.println("It is! so we remove " + rankedKB.get(0).toString());
                combinedRankedKB.removeAll(rankedKB.get(0));
                rankedKB.remove(rankedKB.get(0));
            } else {
                // System.out.println("It is not!");
                break;
            }
        }
        if (combinedRankedKB.size() != 0) {
            // System.out.println("We now check whether or not the formula" +
            // formula.toString() + " is entailed by " + combinedRankedKB.toString());
            if (classicalReasoner.query(combinedRankedKB, formula)) {
                return true;
            } else {
                return false;
            }
        } else {
            // System.out.println("There would then be no ranks remaining, which means the
            // knowledge base entails " + negationOfAntecedent.toString() + ", and thus it
            // entails " + formula.toString() + ", so we know the defeasible counterpart of
            // this implication is also entailed!");
            return true;
        }
    }

    /**
     * Joel's implementation of modified RationalClosure algorithm that utilises
     * indexing to store ranks at which antecedents are no longer exceptional across
     * multiple queries
     * 
     * @param originalRankedKB
     * @param rawQuery
     * @return
     * @throws IOException
     * @throws ParserException
     */
    public boolean RationalClosureJoelRegularIndexing(ArrayList<PlBeliefSet> originalRankedKB, String rawQuery)
            throws ParserException, IOException {

        PlFormula formula = Parser.parseDefeasibleFormula(rawQuery);

        SatSolver.setDefaultSolver(new Sat4jSolver());
        SatReasoner classicalReasoner = new SatReasoner();
        PlFormula negationOfAntecedent = new Negation(((Implication) formula).getFormulas().getFirst());
        ArrayList<PlBeliefSet> rankedKB = (ArrayList<PlBeliefSet>) originalRankedKB.clone();
        PlBeliefSet combinedRankedKB = combine(rankedKB);
        if (antecedentNegationRanksToRemoveFrom.get(negationOfAntecedent) != null) {
            // System.out.println("We know to remove rank " +
            // Integer.toString(antecedentNegationRanksToRemoveFrom.get(negationOfAntecedent))
            // + " and all ranks above it.");
            for (int i = 0; i < (antecedentNegationRanksToRemoveFrom.get(negationOfAntecedent)); i++) {
                rankedKB.remove(rankedKB.get(0));
            }
        } else {
            while (combinedRankedKB.size() != 0) {
                // System.out.println("We are checking whether or not " +
                // negationOfAntecedent.toString()
                // + " is entailed by: " + combinedRankedKB.toString());
                if (classicalReasoner.query(combinedRankedKB, negationOfAntecedent)) {
                    // System.out.println("It is! so we remove " + rankedKB.get(0).toString());
                    combinedRankedKB.removeAll(rankedKB.get(0));
                    rankedKB.remove(rankedKB.get(0));
                } else {
                    // System.out.println("It is not!");
                    antecedentNegationRanksToRemoveFrom.put(negationOfAntecedent,
                            (originalRankedKB.size() - rankedKB.size()));
                    break;
                }
            }
        }

        if (combinedRankedKB.size() != 0) {
            // System.out.println("We now check whether or not the formula" +
            // formula.toString() + " is entailed by "
            // + combinedRankedKB.toString());
            if (classicalReasoner.query(combinedRankedKB, formula)) {
                return true;
            } else {
                return false;
            }
        } else {
            // System.out.println("There would then be no ranks remaining, which means the
            // knowledge base entails "
            // + negationOfAntecedent.toString() + ", and thus it entails " +
            // formula.toString()
            // + ", so we know the defeasible counterpart of this implication is also
            // entailed!");
            return true;
        }
    }

    /**
     * Implementation of Joel's RationalClosure algorithm that utilises
     * Binary Search to find the rank from which all ranks need to be removed, as
     * opposed to iterating linearly from the top, downwards, as in RationalClosure.
     * 
     * @param originalRankedKB
     * @param rawQuery
     * @return
     * @throws IOException
     * @throws ParserException
     */
    public static boolean RationalClosureJoelBinarySearch(ArrayList<PlBeliefSet> originalRankedKB, String rawQuery)
            throws ParserException, IOException {

        PlFormula formula = Parser.parseDefeasibleFormula(rawQuery);

        SatSolver.setDefaultSolver(new Sat4jSolver());
        SatReasoner classicalReasoner = new SatReasoner();

        PlFormula negationOfAntecedent = new Negation(((Implication) formula).getFormulas().getFirst());

        int low = 0;
        int n = originalRankedKB.size();
        int high = n;

        while (high > low) {
            int mid = low + (high - low) / 2;
            List<PlBeliefSet> R = originalRankedKB.subList(mid + 1, n);
            PlBeliefSet combinedRankedKBArray = combine(R);
            if (classicalReasoner.query(combinedRankedKBArray, negationOfAntecedent)) {
                low = mid + 1;
            } else {
                R = originalRankedKB.subList(mid, n);
                combinedRankedKBArray = combine(R);
                if (classicalReasoner.query(combinedRankedKBArray, negationOfAntecedent)) {
                    R = originalRankedKB.subList(mid + 1, n);
                    combinedRankedKBArray = combine(R);
                    return classicalReasoner.query(combinedRankedKBArray, formula);
                } else {
                    high = mid;
                }
            }
        }

        return true;
    }

    /**
     * Implementation of Joel's modified RationalClosure algorithm that utilises
     * Binary Search to find the rank from which all ranks need to be removed, as
     * opposed to iterating linearly from the top, downwards, as in RationalClosure
     * as well as indexing of previous query antecedents.
     * 
     * @param originalRankedKB
     * @param rawQuery
     * @return
     * @throws IOException
     * @throws ParserException
     */
    public boolean RationalClosureJoelBinarySearchIndexing(ArrayList<PlBeliefSet> originalRankedKB, String rawQuery)
            throws ParserException, IOException {

        PlFormula formula = Parser.parseDefeasibleFormula(rawQuery);

        SatSolver.setDefaultSolver(new Sat4jSolver());
        SatReasoner classicalReasoner = new SatReasoner();

        PlFormula negationOfAntecedent = new Negation(((Implication) formula).getFormulas().getFirst());

        int low = 0;
        int n = originalRankedKB.size();
        int high = n;

        Integer removeFrom = antecedentNegationRanksToRemoveFrom.get(negationOfAntecedent);

        if (removeFrom != null) {

            List<PlBeliefSet> R = originalRankedKB.subList(removeFrom, n);
            PlBeliefSet combinedRankedKBArray = combine(R);
            return classicalReasoner.query(combinedRankedKBArray, formula);

        } else {

            while (high > low) {
                int mid = low + (high - low) / 2;
                List<PlBeliefSet> R = originalRankedKB.subList(mid + 1, n);
                PlBeliefSet combinedRankedKBArray = combine(R);
                if (classicalReasoner.query(combinedRankedKBArray, negationOfAntecedent)) {
                    low = mid + 1;
                } else {
                    R = originalRankedKB.subList(mid, n);
                    combinedRankedKBArray = combine(R);
                    if (classicalReasoner.query(combinedRankedKBArray, negationOfAntecedent)) {
                        R = originalRankedKB.subList(mid + 1, n);
                        combinedRankedKBArray = combine(R);
                        return classicalReasoner.query(combinedRankedKBArray, formula);
                    } else {
                        high = mid;
                    }
                }
            }

            return true;
        }
    }

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

}
