package com.mbdr.common.structures;

import org.tweetyproject.logics.pl.syntax.PlBeliefSet;

/**
 * Implementation of a defeasible query set
 */
public class DefeasibleQuerySet extends DefeasibleFormulaCollection{

    /**
     * Default constructor
     */
    public DefeasibleQuerySet(){
        super();
    }

    /**
     * Parameterized constructor
     * 
     * @param collection The collection of formulas
     */
    public DefeasibleQuerySet(DefeasibleFormulaCollection collection){
        super(collection.getDefeasibleKnowledge(), collection.getPropositionalKnowledge());
    }

    /**
     * Parameterized constructor
     * 
     * @param defeasibleKnowledge The defeasible formulas
     * @param propositionalKnowledge The propositional formulas
     */
    public DefeasibleQuerySet(PlBeliefSet defeasibleKnowledge, PlBeliefSet propositionalKnowledge){
        super(defeasibleKnowledge, propositionalKnowledge);
    }
    
}