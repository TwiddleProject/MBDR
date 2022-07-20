package com.mbdr.common.services;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.utils.parsing.Parsing;

public interface DefeasibleReasoner {
    
    default boolean query(String formula){
        if(Parsing.isDefeasible(formula)){
            try{
                Implication defeasibleImplication = (Implication)Parsing.parseDefeasibleFormula(formula);
                return queryDefeasible(defeasibleImplication);
            } catch(Exception e){
                throw new InvalidFormula("Invalid formula for defeasible query.");
            }
        } 
        else {
            try{
                PlFormula propositionalFormula = Parsing.parsePropositionalFormula(formula);
                return queryPropositional(propositionalFormula);
            } catch(Exception e){
                throw new InvalidFormula("Invalid formula for propositional query.");
            }
        }
    }

    boolean queryPropositional(PlFormula formula);
    boolean queryDefeasible(Implication defeasibleImplication);
    void build(DefeasibleKnowledgeBase knowledge);

    public static class InvalidFormula extends RuntimeException {

        public InvalidFormula(){
            super();
        }

        public InvalidFormula(String message){
            super(message);
        }
    }
}
