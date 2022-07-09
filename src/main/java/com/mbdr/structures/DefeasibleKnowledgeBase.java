package com.mbdr.structures;

import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

public class DefeasibleKnowledgeBase {
    
    private PlBeliefSet defeasibleKnowledge;
    private PlBeliefSet propositionalKnowledge;

    public DefeasibleKnowledgeBase(){
        this.defeasibleKnowledge = new PlBeliefSet();
        this.propositionalKnowledge = new PlBeliefSet();
    }

    public DefeasibleKnowledgeBase(PlBeliefSet defeasibleKnowledge, PlBeliefSet propositionalKnowledge){
        this.defeasibleKnowledge = new PlBeliefSet(defeasibleKnowledge);
        this.propositionalKnowledge = new PlBeliefSet(propositionalKnowledge);
    }

    public void addDefeasibleFormula(PlFormula formula){
        this.defeasibleKnowledge.add(formula);
    }

    public void addPropositionalFormula(PlFormula formula){
        this.propositionalKnowledge.add(formula);
    }

    public PlBeliefSet getDefeasibleKnowledge(){
        return new PlBeliefSet(this.defeasibleKnowledge);
    }

    public PlBeliefSet getPropositionalKnowledge(){
        return new PlBeliefSet(this.propositionalKnowledge);
    }

    public PlBeliefSet union(){
        return union(this.propositionalKnowledge, this.defeasibleKnowledge);
    }

    public static PlBeliefSet union(PlBeliefSet a, PlBeliefSet b){
        PlBeliefSet temp = new PlBeliefSet();
        temp.addAll(a);
        temp.addAll(b);
        return temp;
    }

    public String toString(){
        return  "=> : " + this.propositionalKnowledge + "\n" + 
                "~> : " + this.defeasibleKnowledge;
    }

}
