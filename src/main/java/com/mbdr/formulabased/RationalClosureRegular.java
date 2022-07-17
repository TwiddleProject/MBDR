package com.mbdr.formulabased;

import java.util.*;

import org.tweetyproject.logics.pl.syntax.Contradiction;
import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.services.DefeasibleQueryChecker;
import com.mbdr.services.RankConstructor;
import com.mbdr.structures.DefeasibleKnowledgeBase;
import com.mbdr.utils.exceptions.MissingRankConstructor;
import com.mbdr.utils.exceptions.MissingRanking;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.reasoner.*;

public class RationalClosureRegular implements DefeasibleQueryChecker{

    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;

    public RationalClosureRegular(RankConstructor<ArrayList<PlBeliefSet>> constructor){
        this.constructor = constructor;
    }

    public RationalClosureRegular(ArrayList<PlBeliefSet> baseRank){
        this.baseRank = baseRank;
    }

    @Override
    public void build(DefeasibleKnowledgeBase knowledge){
        if(this.constructor == null) throw new MissingRankConstructor("Cannot construct base rank without RankConstructor.");
        this.baseRank = this.constructor.construct(knowledge);
    }

    @Override
    public boolean queryDefeasible(Implication defeasibleImplication){
        if(this.baseRank == null) throw new MissingRanking("Cannot perform query without both base rank and knowledge base.");
        SatSolver.setDefaultSolver(new Sat4jSolver());
        SatReasoner classicalReasoner = new SatReasoner();
        PlFormula negationOfAntecedent = new Negation(defeasibleImplication.getFormulas().getFirst());
        ArrayList<PlBeliefSet> rankedKB = (ArrayList<PlBeliefSet>) this.baseRank.clone();
        PlBeliefSet combinedRankedKB = Utils.combine(rankedKB);
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
            if (classicalReasoner.query(combinedRankedKB, defeasibleImplication)) {
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

    @Override
    public boolean queryPropositional(PlFormula formula){
        return queryDefeasible(new Implication(new Negation(formula), new Contradiction()));
    }

}
