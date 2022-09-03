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
        ArrayList<Set<PlBeliefSet>> subsets = orderedSubsets(knowledge);
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
                    conjunctionDisjunction(subsets.get(i))
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

    private PlFormula conjunctionDisjunction(Set<PlBeliefSet> subsets){
        PlFormula result = new Contradiction();
        for(PlBeliefSet subset : subsets){
            result = new Disjunction(result, new Conjunction(subset));
        }
        return result;
    }

    private ArrayList<Set<PlBeliefSet>> orderedSubsets(DefeasibleKnowledgeBase knowledge){
        PlBeliefSet defeasibleKnowledge = knowledge.getDefeasibleKnowledge();
        int n = defeasibleKnowledge.size();
        ArrayList<Set<PlBeliefSet>> subsets = new ArrayList<>(n);
        HashSet<PlBeliefSet> initial = new HashSet<>();
        initial.add(defeasibleKnowledge);
        // Subset of size n
        subsets.add(initial);
        // For each subset size, descending
        for(int k = n-1; k >= 0; --k){
            Set<PlBeliefSet> currentSubsets = new HashSet<>();
            // For each subset of 1 size larger
            for(PlBeliefSet subset : subsets.get(subsets.size()-1)){
                // Remove each element in turn and add result to current subsets
                for(PlFormula formula : subset){
                    PlBeliefSet current = new PlBeliefSet(subset);
                    current.remove(formula);
                    currentSubsets.add(current);
                }
            }
            // Add currentSubsets to subsets
            subsets.add(currentSubsets);
        }
        return subsets;
    }

}
