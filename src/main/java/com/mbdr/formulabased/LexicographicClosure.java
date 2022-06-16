package com.mbdr.formulabased;

import org.tweetyproject.logics.pl.syntax.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.reasoner.*;

public class LexicographicClosure {

    public static boolean LexicographicClosureDanielNaive(ArrayList<PlBeliefSet> rKB, PlFormula formula)
            throws ParserException, IOException {
        ArrayList<PlBeliefSet> rankedKB = new ArrayList<>(rKB);
        SatReasoner classicalReasoner = new SatReasoner();
        PlParser parser = new PlParser();
        SatSolver.setDefaultSolver(new Sat4jSolver());
        while (rankedKB.size() > 1) {
            // System.out.println("We are checking whether or not "
            // + (new Negation(((Implication) formula).getFormulas().getFirst())).toString()
            // + " is entailed by: "
            // + combine(rankedKB).toString());
            if (classicalReasoner.query(combine(rankedKB),
                    new Negation(((Implication) formula).getFormulas().getFirst()))) {
                if (rankedKB.get(0).size() > 1) {
                    // System.out.println("It does!");
                    // System.out.println(
                    // "Each possible refinement's first formula is subject to removal, and at each
                    // check each subsequent formula is removed until no more removals can be made.
                    // The refinements contain:"
                    // + rankedKB.get(0).toString());
                    Object[] c = rankedKB.get(0).toArray();
                    ArrayList<ArrayList<Object>> rankSet = new ArrayList<>();

                    for (int i = 0; i < c.length; i++) {
                        ArrayList<Object> currRank = new ArrayList<>(Arrays.asList(c));
                        rankSet.add(currRank);
                    }
                    // Checking every subset in a way described in the paper.
                    while (rankSet.get(0).size() > 1) {
                        for (int i = 0; i < rankSet.size(); i++) {

                            ArrayList<Object> temp = rankSet.get(i);
                            if (i > temp.size() - 1) {
                                temp.remove(0);
                            } else {
                                temp.remove(i);
                            }

                            rankSet.set(i, temp);

                            PlBeliefSet tempSet = new PlBeliefSet();
                            for (Object o : temp) {
                                PlFormula tempFormula = (PlFormula) parser.parseFormula(o.toString());
                                tempSet.add(tempFormula);
                            }
                            rankedKB.set(0, tempSet);
                            // System.out.println("tempSet"+tempSet.toString());
                            if (!classicalReasoner.query(combine(rankedKB),
                                    new Negation(((Implication) formula).getFormulas().getFirst()))) {
                                // System.out.println(
                                // (new Negation(((Implication) formula).getFormulas().getFirst())).toString()
                                // + " is not entailed by this refinement.");
                                // System.out.println("We now check whether or not the formula" +
                                // formula.toString()
                                // + " is entailed by " + combine(rankedKB).toString());
                                if (classicalReasoner.query(combine(rankedKB), formula)) {
                                    return true;
                                } else {
                                    return false;
                                }

                            }
                        }
                    }
                    // System.out.println("The remaining statements in our refined ranking do entail
                    // "
                    // + (new Negation(((Implication) formula).getFormulas().getFirst())).toString()
                    // + ". We carry on by removing the top rank.");
                    rankedKB.remove(0);
                } else {
                    // System.out.println("It does! So we remove the top rank.");
                    rankedKB.remove(0);
                }
            } else {
                // System.out.println("It does not!");
                // System.out.println("We now check whether or not the formula " +
                // formula.toString() + " is entailed by "
                // + combine(rankedKB).toString());
                if (classicalReasoner.query(combine(rankedKB), formula)) {
                    return true;
                } else {
                    // System.out.println("The formula " + formula.toString() + " is not entailed by
                    // "
                    // + combine(rankedKB).toString());
                    return false;
                }
            }
        }
        // Since we do not check the refinements of the infinite rank
        // System.out.println("We now check whether or not the formula " +
        // formula.toString() + " is entailed by "
        // + combine(rankedKB).toString());
        if (classicalReasoner.query(combine(rankedKB), formula)) {
            return true;
        } else {
            // System.out.println(
            // "The formula " + formula.toString() + " is not entailed by " +
            // combine(rankedKB).toString());
            return false;
        }

    }

    /**
     * Helper function written by Joel/Daniel to combine ranked PlBeliefSets into
     * single PlBeliefSet
     * 
     * @param ranks
     * @return
     */
    static PlBeliefSet combine(ArrayList<PlBeliefSet> ranks) {
        PlBeliefSet combined = new PlBeliefSet();
        for (PlBeliefSet rank : ranks) {
            combined.addAll(rank);
        }
        return combined;
    }

}
