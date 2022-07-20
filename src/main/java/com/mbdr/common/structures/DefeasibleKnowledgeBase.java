package com.mbdr.common.structures;

import org.tweetyproject.logics.pl.syntax.PlBeliefSet;

public class DefeasibleKnowledgeBase extends DefeasibleFormulaCollection{

    public DefeasibleKnowledgeBase(){
        super();
    }

    public DefeasibleKnowledgeBase(DefeasibleFormulaCollection collection){
        super(collection.getDefeasibleKnowledge(), collection.getPropositionalKnowledge());
    }

    public DefeasibleKnowledgeBase(PlBeliefSet defeasibleKnowledge, PlBeliefSet propositionalKnowledge){
        super(defeasibleKnowledge, propositionalKnowledge);
    }
}
