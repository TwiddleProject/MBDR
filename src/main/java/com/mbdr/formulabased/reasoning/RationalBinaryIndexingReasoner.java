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
 * Implementation of Joel's modified RationalClosure algorithm that utilises
 * Binary Search to find the rank from which all ranks need to be removed, as
 * opposed to iterating linearly from the top, downwards, as in RationalClosure
 * as well as indexing of previous query antecedents.
 */
public class RationalBinaryIndexingReasoner implements DefeasibleReasoner{

    // (For use with Joel's indexing algorithms) Used to store the rank at which a
    // given query is no longer exceptional with the knowledge base
    private HashMap<PlFormula, Integer> antecedentNegationRanksToRemoveFrom = new HashMap<PlFormula, Integer>();
    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;

    /**
     * Default constructor
     */
    public RationalBinaryIndexingReasoner(){
        this(new BaseRank());
    }

    /**
     * Parameterised constructor
     * @param constructor
     */
    public RationalBinaryIndexingReasoner(RankConstructor<ArrayList<PlBeliefSet>> constructor){
        this.antecedentNegationRanksToRemoveFrom = new HashMap<PlFormula, Integer>();
        this.constructor = constructor;
    }

    /**
     * Parameterised constructor
     * @param baseRank
     */
    public RationalBinaryIndexingReasoner(ArrayList<PlBeliefSet> baseRank){
        this.antecedentNegationRanksToRemoveFrom = new HashMap<PlFormula, Integer>();
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
        this.antecedentNegationRanksToRemoveFrom = new HashMap<PlFormula, Integer>();
    }

    /**
     * Answers defeasible query using rational closure with binary search and antecedent indexing optimisations.
     * Code from SCADR (2021).
     * @param defeasibleImplication - defeasible query
     * @return entailment true/false answer
     */
    public boolean queryDefeasible(Implication defeasibleImplication){
        if(this.baseRank == null) throw new MissingRanking("Base rank has not been constructed.");
        SatSolver.setDefaultSolver(new Sat4jSolver());
        SatReasoner classicalReasoner = new SatReasoner();

        PlFormula negationOfAntecedent = new Negation(defeasibleImplication.getFormulas().getFirst());

        int low = 0;
        int n = this.baseRank.size();
        int high = n;

        Integer removeFrom = antecedentNegationRanksToRemoveFrom.get(negationOfAntecedent);

        if (removeFrom != null) {

            List<PlBeliefSet> R = this.baseRank.subList(removeFrom, n);
            PlBeliefSet combinedRankedKBArray = Utils.combine(R);
            return classicalReasoner.query(combinedRankedKBArray, defeasibleImplication);

        } else {

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
    }

    /**
     * Query a propositional formula
     *
     * @param formula The formula to query
     * @return Whether the query is entailed
     */
    @Override
    public boolean queryPropositional(PlFormula formula){
        if(this.baseRank == null) throw new MissingRanking("Base rank has not been constructed.");
        return queryDefeasible(Parsing.normalizePropositionalFormula(formula));
    }

}
