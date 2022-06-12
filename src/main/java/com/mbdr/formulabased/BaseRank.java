package com.mbdr.formulabased;

import java.util.ArrayList;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.utils.parsing.KnowledgeBase;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.reasoner.*;

public class BaseRank {

    /**
     * Standard, unoptimised BaseRank algorithm implementation
     * 
     * @param KB_C
     * @param KB_D
     * @return
     */
    public static ArrayList<PlBeliefSet> BaseRankDirectImplementation(KnowledgeBase knowledge) {
        SatSolver.setDefaultSolver(new Sat4jSolver());
        SatReasoner reasoner = new SatReasoner();
        ArrayList<PlBeliefSet> rankedKB = new ArrayList<PlBeliefSet>();

        PlBeliefSet previousKB = new PlBeliefSet();
        PlBeliefSet currentKB = new PlBeliefSet();

        currentKB.addAll(knowledge.getDefeasibleKnowledge());

        while (!currentKB.equals(previousKB) || !currentKB.isEmpty()) {
            previousKB = currentKB;
            currentKB = new PlBeliefSet();

            // System.out.println("previousKB:\t" + previousKB);
            // System.out.println("currentKB:\t" + currentKB);

            PlBeliefSet KB_C_U_previousKB = KnowledgeBase.union(knowledge.getPropositionalKnowledge(), previousKB);
            // System.out.println("KB_C_U_previousKB:\t" + KB_C_U_previousKB);

            for (PlFormula formula : previousKB) {
                Negation negatedAntecedent = new Negation(((Implication) formula).getFirstFormula());
                // System.out.println("negatedAntecedent:\t" + negatedAntecedent);
                // System.out.println(
                // "reasoner.query(%s, %s)".format(previousKB.toString(),
                // negatedAntecedent.toString()) + ":\t"
                // + reasoner.query(KB_C_U_previousKB, negatedAntecedent));
                if (reasoner.query(KB_C_U_previousKB, negatedAntecedent)) {
                    currentKB.add(formula);
                }
            }

            // System.out.println("currentKB:\t" + currentKB);

            PlBeliefSet currentRank = new PlBeliefSet();
            currentRank.addAll(previousKB);
            currentRank.removeAll(currentKB);
            // System.out.println("currentRank:\t" + currentRank);

            if (!currentRank.isEmpty()) {
                rankedKB.add(currentRank);
            }

        }
        rankedKB.add(knowledge.getPropositionalKnowledge()); // Add all classical statements - infinite rank

        return rankedKB;

    }

}
