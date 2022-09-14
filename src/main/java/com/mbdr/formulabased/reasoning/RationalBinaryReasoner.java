package com.mbdr.formulabased.reasoning;

import java.io.IOException;
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
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.reasoner.*;

/**
 * Implementation of RationalClosure algorithm from SCADR (2021) that utilises
 * Binary Search to find the rank from which all ranks need to be removed, as
 * opposed to iterating linearly from the top, downwards, as in RationalClosure.
 */
public class RationalBinaryReasoner implements DefeasibleReasoner {
    
    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;

    /**
     * Default constructor
     */
    public RationalBinaryReasoner(){
        this(new BaseRank());
    }

    /**
     * Parameterised constructor
     * @param constructor
     */
    public RationalBinaryReasoner(RankConstructor<ArrayList<PlBeliefSet>> constructor){
        this.constructor = constructor;
    }

    /**
     * Parameterised constructor
     * @param baseRank
     */
    public RationalBinaryReasoner(ArrayList<PlBeliefSet> baseRank){
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
     * Answers defeasible query using rational closure with a binary search optimisation.
     * Code from SCADR (2021).
     * @param defeasibleImplication - defeasible query
     * @return entailment true/false answer
     */
    public boolean queryDefeasible(Implication defeasibleImplication){
        if(this.baseRank == null) throw new MissingRanking("Cannot perform query without both base rank and knowledge base.");
        SatSolver.setDefaultSolver(new Sat4jSolver());
        SatReasoner classicalReasoner = new SatReasoner();

        PlFormula negationOfAntecedent = new Negation(defeasibleImplication.getFormulas().getFirst());

        int low = 0;
        int n = this.baseRank.size();
        int high = n;

        while (high > low) {
            int mid = low + (high - low) / 2;
            List<PlBeliefSet> R = this.baseRank.subList(mid + 1, n);
            PlBeliefSet combinedRankedKBArray = Utils.combine(R);
            if (classicalReasoner.query(combinedRankedKBArray, negationOfAntecedent)) {
                low = mid + 1;
            } else {
                R = this.baseRank.subList(mid, n);
                combinedRankedKBArray = Utils.combine(R);
                if (classicalReasoner.query(combinedRankedKBArray, negationOfAntecedent)) {
                    R = this.baseRank.subList(mid + 1, n);
                    combinedRankedKBArray = Utils.combine(R);
                    return classicalReasoner.query(combinedRankedKBArray, defeasibleImplication);
                } else {
                    high = mid;
                }
            }
        }

        return true;
    }

    @Override
    public boolean queryPropositional(PlFormula formula){
        if(this.baseRank == null) throw new MissingRanking("Base rank has not been constructed.");
        return queryDefeasible(Parsing.normalizePropositionalFormula(formula));
    }

}
