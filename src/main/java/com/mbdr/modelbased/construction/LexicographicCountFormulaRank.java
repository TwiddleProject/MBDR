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

public class LexicographicCountFormulaRank implements RankConstructor<RankedFormulasInterpretation>{
    
    private RankedFormulasInterpretation rationalClosureModel;

    public LexicographicCountFormulaRank(){
        this.rationalClosureModel = null;
    }

    public LexicographicCountFormulaRank(RankedFormulasInterpretation rationalClosureModel){
        this.rationalClosureModel = rationalClosureModel;
    }

    public void setRationalClosureModel(RankedFormulasInterpretation rationalClosureModel){
        this.rationalClosureModel = rationalClosureModel;
    }

    /**
     * Refines rational closure model to produce lexicographic model
     * 
     * @param knowledge            The knowledge base
     * @return The model for lexicographic closure
     */
    public RankedFormulasInterpretation construct(DefeasibleKnowledgeBase knowledge) {
        if(this.rationalClosureModel == null){
            this.rationalClosureModel = new FormulaRank().construct(knowledge);
        }
        RankedFormulasInterpretation rankedModel = new RankedFormulasInterpretation(0);
        Sat4jSolver reasoner = new Sat4jSolver();
        ArrayList<Set<PlBeliefSet>> subsets = LexicographicWeakeningReasoner.orderedSubsets(knowledge);
        // For each rational closure rank
        for(int rank = 0; rank < this.rationalClosureModel.getRankCount(); ++rank){
            PlFormula cumulative = new Tautology();
            PlFormula rankFormula = this.rationalClosureModel.getRank(rank);
            // For each subset size
            // System.out.println("Rank: " + rank);
            // System.out.println("Rank Formula: " + rankFormula);
            // System.out.println("Rank models: " + Utils.getModels(rankFormula, knowledge.union().getSignature()));
            for(int i = 0; i < subsets.size(); ++i){
                // Fr_i = Fr && ~(Fr_0 || Fr_1 || ... || Fr_i-1) && AtLeastOneSubsetOfSize(n-i)
                PlFormula refined = new Conjunction(new HashSet<>(Arrays.asList(
                    rankFormula, 
                    cumulative, 
                    LexicographicWeakeningReasoner.conjunctionDisjunction(subsets.get(i))
                )));
                // System.out.println("Rank: " + rank + " " + "Refined: " + i);
                // System.out.println("Refined: " + refined);
                // System.out.println("RC: " + rankFormula);
                // System.out.println("Cumulative: " + cumulative);
                // System.out.println(Utils.getModels(refined, knowledge.union().getSignature()));
                // If contains worlds
                if(reasoner.isConsistent(refined)){
                    rankedModel.addRank(refined);
                    cumulative = new Conjunction(new Negation(refined), cumulative);
                }
                
            }
        }
        rankedModel.setInfiniteRank(this.rationalClosureModel.getInfiniteRank());
        return rankedModel;
    }

}
