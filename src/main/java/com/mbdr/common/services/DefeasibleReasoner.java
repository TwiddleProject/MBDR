package com.mbdr.common.services;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.utils.parsing.Parsing;

/**
 * Interface for constructing and querying a reasoner
 */
public interface DefeasibleReasoner {
    
    /**
     * Query an unparsed string formula
     * 
     * @param formula The string representation of the formula
     * @return The query result
     */
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

    /**
     * Query a propositional formula
     * 
     * @param formula The propositional formula
     * @return The query result
     */
    boolean queryPropositional(PlFormula formula);

    /**
     * Query a defeasible implication
     * 
     * @param formula The defeasible implication
     * @return The query result
     */
    boolean queryDefeasible(Implication defeasibleImplication);

    /**
     * Builds reasoner backend
     * 
     * @param knowledge The knowledge base
     */
    void build(DefeasibleKnowledgeBase knowledge);

    /**
     * Invalid formula exception
     */
    public static class InvalidFormula extends RuntimeException {

        /**
         * Default constructor
         */
        public InvalidFormula(){
            super();
        }

        /**
         * Parameterized constructor
         * 
         * @param message The error message
         */
        public InvalidFormula(String message){
            super(message);
        }
    }
}
