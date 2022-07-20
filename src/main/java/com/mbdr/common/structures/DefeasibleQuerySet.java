package com.mbdr.common.structures;

import org.tweetyproject.logics.pl.syntax.PlBeliefSet;

public class DefeasibleQuerySet extends DefeasibleFormulaCollection{

    public DefeasibleQuerySet(){
        super();
    }

    public DefeasibleQuerySet(DefeasibleFormulaCollection collection){
        super(collection.getDefeasibleKnowledge(), collection.getPropositionalKnowledge());
    }

    public DefeasibleQuerySet(PlBeliefSet defeasibleKnowledge, PlBeliefSet propositionalKnowledge){
        super(defeasibleKnowledge, propositionalKnowledge);
    }
    
}