package com.mbdr.common.structures;

import org.tweetyproject.logics.pl.syntax.PlBeliefSet;

/**
 * Implementation of a defeasible knowledge base (may contain proposition knowledge)
 */
public class DefeasibleKnowledgeBase extends DefeasibleFormulaCollection{

    /**
     * Default constructor
     */
    public DefeasibleKnowledgeBase(){
        super();
    }

    /**
     * Parameterized constructor
     * 
     * @param collection The defeasible formula collection
     */
    public DefeasibleKnowledgeBase(DefeasibleFormulaCollection collection){
        super(collection.getDefeasibleKnowledge(), collection.getPropositionalKnowledge());
    }

    /**
     * Parameterized constructor
     * 
     * @param defeasibleKnowledge The defeasible knowledge
     * @param propositionalKnowledge The propositional knowledge
     */
    public DefeasibleKnowledgeBase(PlBeliefSet defeasibleKnowledge, PlBeliefSet propositionalKnowledge){
        super(defeasibleKnowledge, propositionalKnowledge);
    }
}
