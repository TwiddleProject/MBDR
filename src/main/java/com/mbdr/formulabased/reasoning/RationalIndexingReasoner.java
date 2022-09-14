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
 * Implementation of modified RationalClosure algorithm from SCADR (2021) that utilises
 * indexing to store ranks at which antecedents are no longer exceptional across
 * multiple queries.
 */
public class RationalIndexingReasoner implements DefeasibleReasoner {
    

    // Used to store the rank at which a given query is no longer exceptional with the knowledge base
    private HashMap<PlFormula, Integer> antecedentNegationRanksToRemoveFrom;
    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;

    /**
     * Default constructor
     */
    public RationalIndexingReasoner(){
        this(new BaseRank());
    }

    /**
     * Parameterised constructor
     * @param constructor
     */
    public RationalIndexingReasoner(RankConstructor<ArrayList<PlBeliefSet>> constructor){
        this.antecedentNegationRanksToRemoveFrom = new HashMap<PlFormula, Integer>();
        this.constructor = constructor;
    }

    /**
     * Parameterised constructor
     * @param baseRank
     */
    public RationalIndexingReasoner(ArrayList<PlBeliefSet> baseRank){
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
    }


    /**
     * Answers defeasible query using modified RationalClosure algorithm that indexes ranks of previously found antecedents.
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
        if (antecedentNegationRanksToRemoveFrom.get(negationOfAntecedent) != null) {
            for (int i = 0; i < (antecedentNegationRanksToRemoveFrom.get(negationOfAntecedent)); i++) {
                rankedKB.remove(rankedKB.get(0));
            }
        } else {
            while (combinedRankedKB.size() != 0) {
                if (classicalReasoner.query(combinedRankedKB, negationOfAntecedent)) {
                    combinedRankedKB.removeAll(rankedKB.get(0));
                    rankedKB.remove(rankedKB.get(0));
                } else {
                    antecedentNegationRanksToRemoveFrom.put(negationOfAntecedent,
                            (this.baseRank.size() - rankedKB.size()));
                    break;
                }
            }
        }

        if (combinedRankedKB.size() != 0) {
            if (classicalReasoner.query(combinedRankedKB, defeasibleImplication)) {
                return true;
            } else {
                return false;
            }
        } else {
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
