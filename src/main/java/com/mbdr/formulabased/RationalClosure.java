package com.mbdr.formulabased;

import java.util.*;
import java.util.Map.Entry;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.syntax.PlSignature;

import com.mbdr.utils.Parsing;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.reasoner.*;

public class RationalClosure {

    /**
     * Standard, unoptimised RationalClosure algorithm implementation
     * 
     * @param KB_C     - Knowledge base containing all the purely classical formulas
     *                 of the given knowledge base
     * @param KB_D     - Knowledge base containing all the DIs of the given
     *                 knowledge base
     * @param query_DI
     * @return
     */
    public static boolean RationalClosureDirectImplementation(PlBeliefSet KB_C, PlBeliefSet KB_D, Implication query_DI) {
        SatSolver.setDefaultSolver(new Sat4jSolver());
        SatReasoner reasoner = new SatReasoner();

        ArrayList<PlBeliefSet> ranked_KB = BaseRank.BaseRankDirectImplementation(KB_C, KB_D);
        PlBeliefSet R = new PlBeliefSet(KB_D);
        Negation query_negated_antecedent = new Negation(query_DI.getFirstFormula());

        int i = 0;
        while (reasoner.query(Parsing.Union(KB_C, R), query_negated_antecedent) && !R.isEmpty()) {
            R.removeAll(ranked_KB.get(i));
            i++;
        }

        return reasoner.query(Parsing.Union(KB_C, R), query_DI);
    }

}
