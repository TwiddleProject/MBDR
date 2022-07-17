package com.mbdr.formulabased;

import java.io.IOException;
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

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.reasoner.*;

public class RationalClosureBinaryIndexing implements DefeasibleQueryChecker{

    // (For use with Joel's indexing algorithms) Used to store the rank at which a
    // given query is no longer exceptional with the knowledge base
    private HashMap<PlFormula, Integer> antecedentNegationRanksToRemoveFrom = new HashMap<PlFormula, Integer>();
    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;

    public RationalClosureBinaryIndexing(RankConstructor<ArrayList<PlBeliefSet>> constructor){
        this.antecedentNegationRanksToRemoveFrom = new HashMap<PlFormula, Integer>();
        this.constructor = constructor;
    }

    public RationalClosureBinaryIndexing(ArrayList<PlBeliefSet> baseRank){
        this.antecedentNegationRanksToRemoveFrom = new HashMap<PlFormula, Integer>();
        this.baseRank = baseRank;
    }

    @Override
    public void build(DefeasibleKnowledgeBase knowledge){
        if(this.constructor == null) throw new MissingRankConstructor("Cannot construct base rank without RankConstructor.");
        this.baseRank = this.constructor.construct(knowledge);
        this.antecedentNegationRanksToRemoveFrom = new HashMap<PlFormula, Integer>();
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
    public boolean queryDefeasible(Implication defeasibleImplication){

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

    @Override
    public boolean queryPropositional(PlFormula formula){
        return queryDefeasible(new Implication(new Negation(formula), new Contradiction()));
    }

}
