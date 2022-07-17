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
import com.mbdr.utils.exceptions.MissingRanking;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.reasoner.*;

public class RationalIndexingChecker implements DefeasibleQueryChecker {
    

    // (For use with Joel's indexing algorithms) Used to store the rank at which a
    // given query is no longer exceptional with the knowledge base
    private HashMap<PlFormula, Integer> antecedentNegationRanksToRemoveFrom;
    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;

    public RationalIndexingChecker(RankConstructor<ArrayList<PlBeliefSet>> constructor){
        this.antecedentNegationRanksToRemoveFrom = new HashMap<PlFormula, Integer>();
        this.constructor = constructor;
    }

    public RationalIndexingChecker(ArrayList<PlBeliefSet> baseRank){
        this.antecedentNegationRanksToRemoveFrom = new HashMap<PlFormula, Integer>();
        this.baseRank = baseRank;
    }

    @Override
    public void build(DefeasibleKnowledgeBase knowledge){
        if(this.constructor == null) throw new MissingRankConstructor("Cannot construct base rank without RankConstructor.");
        this.baseRank = this.constructor.construct(knowledge);
    }

    /**
     * Joel's implementation of modified RationalClosure algorithm that utilises
     * indexing to store ranks at which antecedents are no longer exceptional across
     * multiple queries
     * 
     * @param originalRankedKB
     * @param rawQuery
     * @return
     * @throws IOException
     * @throws ParserException
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
            // System.out.println("We know to remove rank " +
            // Integer.toString(antecedentNegationRanksToRemoveFrom.get(negationOfAntecedent))
            // + " and all ranks above it.");
            for (int i = 0; i < (antecedentNegationRanksToRemoveFrom.get(negationOfAntecedent)); i++) {
                rankedKB.remove(rankedKB.get(0));
            }
        } else {
            while (combinedRankedKB.size() != 0) {
                // System.out.println("We are checking whether or not " +
                // negationOfAntecedent.toString()
                // + " is entailed by: " + combinedRankedKB.toString());
                if (classicalReasoner.query(combinedRankedKB, negationOfAntecedent)) {
                    // System.out.println("It is! so we remove " + rankedKB.get(0).toString());
                    combinedRankedKB.removeAll(rankedKB.get(0));
                    rankedKB.remove(rankedKB.get(0));
                } else {
                    // System.out.println("It is not!");
                    antecedentNegationRanksToRemoveFrom.put(negationOfAntecedent,
                            (this.baseRank.size() - rankedKB.size()));
                    break;
                }
            }
        }

        if (combinedRankedKB.size() != 0) {
            // System.out.println("We now check whether or not the formula" +
            // formula.toString() + " is entailed by "
            // + combinedRankedKB.toString());
            if (classicalReasoner.query(combinedRankedKB, defeasibleImplication)) {
                return true;
            } else {
                return false;
            }
        } else {
            // System.out.println("There would then be no ranks remaining, which means the
            // knowledge base entails "
            // + negationOfAntecedent.toString() + ", and thus it entails " +
            // formula.toString()
            // + ", so we know the defeasible counterpart of this implication is also
            // entailed!");
            return true;
        }
    }

    @Override
    public boolean queryPropositional(PlFormula formula){
        return queryDefeasible(new Implication(new Negation(formula), new Contradiction()));
    }
}
