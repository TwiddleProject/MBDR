package com.mbdr.modelbased.construction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.tweetyproject.logics.pl.syntax.Tautology;
import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;
import org.tweetyproject.logics.pl.syntax.Conjunction;
import org.tweetyproject.logics.pl.syntax.Contradiction;
import org.tweetyproject.logics.pl.syntax.Disjunction;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.formulabased.Utils;
import com.mbdr.formulabased.construction.BaseRank;
import com.mbdr.modelbased.structures.RankedFormulasInterpretation;
import com.mbdr.formulabased.reasoning.LexicographicWeakeningReasoner;

public class LexicographicCountCumulativeFormulaRank implements RankConstructor<RankedFormulasInterpretation>{
    
    private RankedFormulasInterpretation cumulativeRationalClosureModel;

    public LexicographicCountCumulativeFormulaRank(){
        this.cumulativeRationalClosureModel = null;
    }

    public LexicographicCountCumulativeFormulaRank(RankedFormulasInterpretation rationalClosureModel){
        this.cumulativeRationalClosureModel = rationalClosureModel;
    }

    public void setRationalClosureModel(RankedFormulasInterpretation rationalClosureModel){
        this.cumulativeRationalClosureModel = rationalClosureModel;
    }

    /**
     * Refines rational closure model to produce lexicographic model
     * 
     * @param knowledge            The knowledge base
     * @return The model for lexicographic closure
     */
    public RankedFormulasInterpretation construct(DefeasibleKnowledgeBase knowledge) {
        if(this.cumulativeRationalClosureModel == null){
            this.cumulativeRationalClosureModel = new CumulativeFormulaRank().construct(knowledge);
        }
        RankedFormulasInterpretation cumulativeModel = new RankedFormulasInterpretation(0);
        Sat4jSolver reasoner = new Sat4jSolver();
        ArrayList<Set<PlBeliefSet>> subsets = LexicographicWeakeningReasoner.orderedSubsets(knowledge);
        // For each rational closure rank
        cumulativeModel.addRank(this.cumulativeRationalClosureModel.getRank(0));
        for(int rank = 1; rank < this.cumulativeRationalClosureModel.getRankCount(); ++rank){
            PlFormula prevRankFormula = this.cumulativeRationalClosureModel.getRank(rank-1);
            PlFormula rankFormula = this.cumulativeRationalClosureModel.getRank(rank);
            // For each subset size
            // System.out.println("Rank: " + rank);
            // System.out.println("Rank Formula: " + rankFormula);
            // System.out.println("Rank models: " + Utils.getModels(rankFormula, knowledge.union().getSignature()));
            for(int i = 0; i < subsets.size(); ++i){
                // Fr_i = Fr && ~(Fr_0 || Fr_1 || ... || Fr_i-1) && AtLeastOneSubsetOfSize(n-i)
                PlFormula refined = new Disjunction(
                    prevRankFormula,
                    new Conjunction(
                        rankFormula,
                        LexicographicWeakeningReasoner.conjunctionDisjunction(subsets.get(i))
                    )
                );
                // System.out.println("Rank: " + rank + " " + "Refined: " + i);
                // System.out.println("Refined: " + refined);
                // System.out.println("RC: " + rankFormula);
                // System.out.println("Cumulative: " + cumulative);
                // System.out.println(Utils.getModels(refined, knowledge.union().getSignature()));
                // If contains worlds   
                if(reasoner.isSatisfiable(
                    new Conjunction(refined, new Negation(cumulativeModel.getRank(cumulativeModel.getRankCount()-1))))){
                    cumulativeModel.addRank(refined);
                }
                
            }
        }
        cumulativeModel.setInfiniteRank(this.cumulativeRationalClosureModel.getInfiniteRank());
        return cumulativeModel;
    }

}
