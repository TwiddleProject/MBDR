package com.mbdr.common.structures;

import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

/**
 * Represents a heterogenous mix of propositional and defeasible
 * formulas
 */
public class DefeasibleFormulaCollection {

    private PlBeliefSet defeasibleKnowledge;
    private PlBeliefSet propositionalKnowledge;

    /**
     * Default constructor
     */
    public DefeasibleFormulaCollection() {
        this.defeasibleKnowledge = new PlBeliefSet();
        this.propositionalKnowledge = new PlBeliefSet();
    }

    /**
     * Parameterized constructor
     * 
     * @param defeasibleKnowledge The defeasible knowledge
     * @param propositionalKnowledge The propositional knowledge
     */
    public DefeasibleFormulaCollection(PlBeliefSet defeasibleKnowledge, PlBeliefSet propositionalKnowledge) {
        this.defeasibleKnowledge = new PlBeliefSet(defeasibleKnowledge);
        this.propositionalKnowledge = new PlBeliefSet(propositionalKnowledge);
    }

    /**
     * Add defeasible formula
     * 
     * @param formula The defeasible formula
     */
    public void addDefeasibleFormula(PlFormula formula) {
        this.defeasibleKnowledge.add(formula);
    }

    /**
     * Add propositional formula
     * 
     * @param formula The propositional formula
     */
    public void addPropositionalFormula(PlFormula formula) {
        this.propositionalKnowledge.add(formula);
    }

    /**
     * Get copy of defeasible knowledge
     * 
     * @return The defeasible knowledge
     */
    public PlBeliefSet getDefeasibleKnowledge() {
        return new PlBeliefSet(this.defeasibleKnowledge);
    }

    /**
     * Get copy of propositional knowledge
     * 
     * @return The propositional knowledge
     */
    public PlBeliefSet getPropositionalKnowledge() {
        return new PlBeliefSet(this.propositionalKnowledge);
    }

    /**
     * Get union of propositional and materialized defeasible knowledge
     * 
     * @return The union of the knowledge
     */
    public PlBeliefSet union() {
        return union(this.propositionalKnowledge, this.defeasibleKnowledge);
    }

    /**
     * Get the union of two formula sets
     * 
     * @param a The first set of formulas
     * @param b The second set of formulas
     * @return The union
     */
    public static PlBeliefSet union(PlBeliefSet a, PlBeliefSet b) {
        PlBeliefSet temp = new PlBeliefSet();
        temp.addAll(a);
        temp.addAll(b);
        return temp;
    }

    /**
     * Overriden toString method
     */
    public String toString() {
        return "=> : " + this.propositionalKnowledge + "\n" +
                "~> : " + this.defeasibleKnowledge;
    }

}
