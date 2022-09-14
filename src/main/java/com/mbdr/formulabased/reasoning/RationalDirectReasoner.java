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
import com.mbdr.formulabased.construction.BaseRank;
import com.mbdr.utils.parsing.Parsing;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.reasoner.*;

/**
 * Standard, unoptimised RationalClosure algorithm implementation that directly resembles abstract algorithm definition.
 */
public class RationalDirectReasoner implements DefeasibleReasoner{

    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;
    private DefeasibleKnowledgeBase knowledge;

    /**
     * Default constructor
     */
    public RationalDirectReasoner(){
        this(new BaseRank());
    }

    public RationalDirectReasoner(RankConstructor<ArrayList<PlBeliefSet>> constructor){
        this.constructor = constructor;
    }

    public RationalDirectReasoner(ArrayList<PlBeliefSet> baseRank, DefeasibleKnowledgeBase knowledge){
        this.baseRank = baseRank;
        this.knowledge = knowledge;
    }

    /**
     * Gets the base ranking of the knowledge base using BaseRank implementation
     * @param knowledge - defeasible knowledge base
     */
    @Override
    public void build(DefeasibleKnowledgeBase knowledge){
        if(this.constructor == null) throw new MissingRankConstructor("Cannot construct base rank without RankConstructor.");
        this.baseRank = this.constructor.construct(knowledge);
        this.knowledge = knowledge;
    }

    /**
     * Answers defeasible query using RationalClosure algorithm
     * @param defeasibleImplication - defeasible query
     * @return entailment true/false answer
     */
    public boolean queryDefeasible(Implication defeasibleImplication) {
        if(this.baseRank == null || this.knowledge == null) throw new MissingRanking("Cannot perform query without both base rank and knowledge base.");
        SatSolver.setDefaultSolver(new Sat4jSolver());
        SatReasoner reasoner = new SatReasoner();
        PlBeliefSet R = new PlBeliefSet(this.knowledge.getDefeasibleKnowledge());
        Negation query_negated_antecedent = new Negation(defeasibleImplication.getFirstFormula());

        int i = 0;
        while (reasoner.query(DefeasibleKnowledgeBase.union(this.knowledge.getPropositionalKnowledge(), R), query_negated_antecedent)
                && !R.isEmpty()) {
            R.removeAll(this.baseRank.get(i));
            i++;
        }

        return reasoner.query(DefeasibleKnowledgeBase.union(knowledge.getPropositionalKnowledge(), R), defeasibleImplication);
    }

    @Override
    public boolean queryPropositional(PlFormula formula){
        if(this.baseRank == null) throw new MissingRanking("Base rank has not been constructed.");
        return queryDefeasible(Parsing.normalizePropositionalFormula(formula));
    }
}
