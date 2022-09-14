package com.mbdr.formulabased.reasoning;

import java.util.*;

import org.tweetyproject.logics.pl.syntax.Contradiction;
import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.exceptions.MissingRankConstructor;
import com.mbdr.common.exceptions.MissingRanking;
import com.mbdr.common.services.DefeasibleReasoner;
import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.formulabased.Utils;
import com.mbdr.formulabased.construction.BaseRank;
import com.mbdr.utils.parsing.Parsing;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.reasoner.*;


/**
 * Implementation of standard, unoptimised RationalClosure algorithm from SCADR (2021).
 */
public class RationalRegularReasoner implements DefeasibleReasoner{

    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;

    /**
     * Default constructor
     */
    public RationalRegularReasoner(){
        this(new BaseRank());
    }

    /**
     * Parameterised constructor
     * @param constructor
     */
    public RationalRegularReasoner(RankConstructor<ArrayList<PlBeliefSet>> constructor){
        this.constructor = constructor;
    }

    /**
     * Parameterised constructor
     * @param baseRank
     */
    public RationalRegularReasoner(ArrayList<PlBeliefSet> baseRank){
        this.baseRank = baseRank;
    }

    /**
     * Gets the base ranking of the knowledge base using BaseRank implementation
     * @param knowledge - defeasible knowledge base
     */
    @Override
    public void build(DefeasibleKnowledgeBase knowledge){
        if(this.constructor == null) throw new MissingRankConstructor("Cannot construct base rank without RankConstructor.");
        this.baseRank = this.constructor.construct(knowledge);
    }

    /**
     * Answers defeasible query using standard, unoptimised RationalClosure algorithm.
     * Code from SCADR (2021).
     * @param defeasibleImplication - defeasible query
     * @return entailment true/false answer
     */
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
        if(this.baseRank == null) throw new MissingRanking("Base rank has not been constructed.");
        return queryDefeasible(Parsing.normalizePropositionalFormula(formula));
    }

}
