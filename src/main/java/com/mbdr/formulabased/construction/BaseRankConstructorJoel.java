package com.mbdr.formulabased.construction;

import java.util.ArrayList;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.reasoner.*;

public class BaseRankConstructorJoel implements RankConstructor<ArrayList<PlBeliefSet>>{
    
    /**
     * NB this needs to be tested - not sure if producing correct results
     */
    public ArrayList<PlBeliefSet> construct(DefeasibleKnowledgeBase knowledgeBase) {
        SatSolver.setDefaultSolver(new Sat4jSolver());
        SatReasoner reasoner = new SatReasoner();
        ArrayList<PlBeliefSet> rankedKB = new ArrayList<PlBeliefSet>();
        PlBeliefSet currentMaterialisation = knowledgeBase.getDefeasibleKnowledge();
        PlBeliefSet prevMaterialisation = new PlBeliefSet();

        while (!currentMaterialisation.equals(prevMaterialisation)) {
            prevMaterialisation = currentMaterialisation;
            // System.out.println("prevMaterialisation: \t" + prevMaterialisation);
            currentMaterialisation = new PlBeliefSet();
            for (PlFormula f : prevMaterialisation) {
                if (f.toString().contains("=>")) {
                    if (reasoner.query(prevMaterialisation, new Negation(((Implication) f).getFormulas().getFirst()))) {
                        currentMaterialisation.add(f);
                    }
                }
            }
            // System.out.println("currentMaterialisation:\t" + currentMaterialisation);
            PlBeliefSet newRank = new PlBeliefSet();
            for (PlFormula form : prevMaterialisation) {
                if (!knowledgeBase.getPropositionalKnowledge().contains(form)) {
                    newRank.add(form);
                }
            }
            newRank.removeAll(currentMaterialisation);
            // System.out.println("newRank:\t" + newRank);
            if (newRank.size() != 0) {
                rankedKB.add(newRank);
                System.out.println("Added rank " + Integer.toString(rankedKB.size() - 1));
            } else {
                knowledgeBase.getPropositionalKnowledge().addAll(currentMaterialisation);
            }
        }
        rankedKB.add(knowledgeBase.getPropositionalKnowledge());
        System.out.println("Base Ranking of Knowledge Base:");
        for (PlBeliefSet rank : rankedKB) {
            if (rankedKB.indexOf(rank) == rankedKB.size() - 1) {
                System.out.println("Infinite Rank:" + rank.toString());
            } else {
                System.out.println("Rank " + Integer.toString(rankedKB.indexOf(rank)) + ":" + rank.toString());
            }
        }
        return rankedKB;
    }
}
