package com.mbdr.modelbased;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.utils.parsing.Parser;

public class EntailmentChecker {
    
    private ArrayList<Set<NicePossibleWorld>> model;

    public EntailmentChecker(ArrayList<Set<NicePossibleWorld>> model){
        this.model = model;
    }

    public void setModel(ArrayList<Set<NicePossibleWorld>> model){
        this.model = model;
    }

    private boolean checkMinimalWorlds(Implication defeasibleFormula){
        boolean foundMinRank = false;
        int minRank = -1;
        rankLoop:
        for(int i = 0; i < this.model.size()-1; ++i){
            for(NicePossibleWorld world : this.model.get(i)){
                if(world.satisfies(defeasibleFormula.getFirstFormula())){
                    minRank = i;
                    foundMinRank = true;
                    break rankLoop;
                }
            }
        }
        if(!foundMinRank) {
            return true;
        }
        for(NicePossibleWorld world : this.model.get(minRank)){
            if(world.satisfies(defeasibleFormula.getFirstFormula()) && !world.satisfies(defeasibleFormula.getSecondFormula())){
                return false;
            }
        }
        return true;
    }

    public boolean query(String formula) throws ParserException, IOException{
        if(Parser.isDefeasible(formula)){
            PlFormula defeasibleFormula = Parser.parseDefeasibleFormula(formula);
            return checkMinimalWorlds((Implication) defeasibleFormula);
        } 
        else {
            PlFormula propositionalFormula = Parser.parsePropositionalFormula(formula);
            for(int i=0; i < this.model.size()-1; ++i){
                for(NicePossibleWorld world : this.model.get(i)){
                    if(!world.satisfies(propositionalFormula)){
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
