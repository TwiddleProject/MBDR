package com.mbdr.modelbased.construction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.tweetyproject.logics.pl.syntax.Tautology;
import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.syntax.Conjunction;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.modelbased.structures.RankedFormulasInterpretation;
import com.mbdr.formulabased.reasoning.LexicographicWeakeningReasoner;

/**
 * Implementation of the LexicographicFormulaRank algorith in 
 * Lexicographic Model-based Defeasible Reasoning
 */
public class LexicographicCountFormulaRank implements RankConstructor<RankedFormulasInterpretation>{
    
    private RankedFormulasInterpretation rationalClosureModel;

    /**
     * Default constructor
     */
    public LexicographicCountFormulaRank(){
        this.rationalClosureModel = null;
    }

    /**
     * Parameterized constructor
     * 
     * @param rationalClosureModel The formula ranked model to refine
     */
    public LexicographicCountFormulaRank(RankedFormulasInterpretation rationalClosureModel){
        this.rationalClosureModel = rationalClosureModel;
    }

    /**
     * Formula model rank setter
     * 
     * @param rationalClosureModel The formula ranked model to refine
     */
    public void setRationalClosureModel(RankedFormulasInterpretation rationalClosureModel){
        this.rationalClosureModel = rationalClosureModel;
    }

    /**
     * Refines rational closure model to produce lexicographic model
     * 
     * @param knowledge The knowledge base
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
            for(int i = 0; i < subsets.size(); ++i){
                // Fl_i = Fr_i && ~(Fl_0 || Fl_1 || ... || Fl_i-1) && AtLeastOneSubsetOfSize(n-i)
                PlFormula refined = new Conjunction(new HashSet<>(Arrays.asList(
                    rankFormula, 
                    cumulative, 
                    LexicographicWeakeningReasoner.conjunctionDisjunction(subsets.get(i))
                )));
                // If contains worlds
                if(reasoner.isConsistent(refined)){
                    rankedModel.addRank(refined);
                    cumulative = new Conjunction(new Negation(refined), cumulative);
                }
            }
        }
        // Fl_inf = Fr_inf
        rankedModel.setInfiniteRank(this.rationalClosureModel.getInfiniteRank());
        return rankedModel;
    }

}
