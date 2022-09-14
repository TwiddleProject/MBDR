package com.mbdr.modelbased.construction;

import java.util.ArrayList;
import java.util.Set;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.syntax.Conjunction;
import org.tweetyproject.logics.pl.syntax.Disjunction;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.modelbased.structures.RankedFormulasInterpretation;
import com.mbdr.formulabased.reasoning.LexicographicWeakeningReasoner;

/**
 * Implementation of LexicographicCumulativeFormulaRank algorithm in 
 * Lexicographic Model-based Defeasible Reasoning
 */
public class LexicographicCountCumulativeFormulaRank implements RankConstructor<RankedFormulasInterpretation>{
    
    private RankedFormulasInterpretation cumulativeRationalClosureModel;

    /**
     * Default constructor
     */
    public LexicographicCountCumulativeFormulaRank(){
        this.cumulativeRationalClosureModel = null;
    }

    /**
     * Parameterized constructor
     * 
     * @param rationalClosureModel The cumulative formula model to refine
     */
    public LexicographicCountCumulativeFormulaRank(RankedFormulasInterpretation rationalClosureModel){
        this.cumulativeRationalClosureModel = rationalClosureModel;
    }

    /**
     * Cumulative formula model setter
     * 
     * @param rationalClosureModel The cumulative formula model to refine
     */
    public void setRationalClosureModel(RankedFormulasInterpretation rationalClosureModel){
        this.cumulativeRationalClosureModel = rationalClosureModel;
    }

    /**
     * Refines rational closure model to produce lexicographic model
     * 
     * @param knowledge The knowledge base
     * @return The cumulative formula model for lexicographic closure
     */
    public RankedFormulasInterpretation construct(DefeasibleKnowledgeBase knowledge) {
        if(this.cumulativeRationalClosureModel == null){
            this.cumulativeRationalClosureModel = new CumulativeFormulaRank().construct(knowledge);
        }
        RankedFormulasInterpretation cumulativeModel = new RankedFormulasInterpretation(0);
        Sat4jSolver reasoner = new Sat4jSolver();
        ArrayList<Set<PlBeliefSet>> subsets = LexicographicWeakeningReasoner.orderedSubsets(knowledge);
        // No need to refine bottom rank
        cumulativeModel.addRank(this.cumulativeRationalClosureModel.getRank(0));
        // For each rational closure rank
        for(int rank = 1; rank < this.cumulativeRationalClosureModel.getRankCount(); ++rank){
            PlFormula prevRankFormula = this.cumulativeRationalClosureModel.getRank(rank-1);
            PlFormula rankFormula = this.cumulativeRationalClosureModel.getRank(rank);
            // For each subset size
            for(int i = 0; i < subsets.size(); ++i){
                // Fl_i = Fr_prev || (Fr_curr && AtLeastOneSubsetOfSize(n-i))
                PlFormula refined = new Disjunction(
                    prevRankFormula,
                    new Conjunction(
                        rankFormula,
                        LexicographicWeakeningReasoner.conjunctionDisjunction(subsets.get(i))
                    )
                );
                // If contains worlds (Fl_i not entails Fl_prev)   
                if(reasoner.isSatisfiable(
                    new Conjunction(refined, new Negation(cumulativeModel.getRank(cumulativeModel.getRankCount()-1))))){
                    cumulativeModel.addRank(refined);
                }
            }
        }
        // Same infinite rank
        cumulativeModel.setInfiniteRank(this.cumulativeRationalClosureModel.getInfiniteRank());
        return cumulativeModel;
    }

}
