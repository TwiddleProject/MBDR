package com.mbdr.modelbased;

import java.io.IOException;

import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.utils.parsing.Parser;
import com.mbdr.structures.RankedInterpretation;

public class EntailmentChecker {
    
    private RankedInterpretation model;

    public EntailmentChecker(RankedInterpretation model){
        this.model = model;
    }

    public void setModel(RankedInterpretation model){
        this.model = model;
    }

    private boolean checkMinimalWorlds(Implication defeasibleFormula){
        boolean foundMinRank = false;
        for(int i = 0; i < this.model.getRankCount(); ++i){
            for(NicePossibleWorld world : this.model.getRank(i)){
                if(world.satisfies(defeasibleFormula.getFirstFormula())){
                    foundMinRank = true;
                    if(!world.satisfies(defeasibleFormula.getSecondFormula())){
                        return false;
                    }
                }
            }
            if(foundMinRank){
                return true;
            }
        }
        return true;
    }

    private boolean checkAllWorlds(PlFormula propositionalFormula){
        for(int i=0; i < this.model.getRankCount(); ++i){
            for(NicePossibleWorld world : this.model.getRank(i)){
                if(!world.satisfies(propositionalFormula)){
                    return false;
                }
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
            return checkAllWorlds(propositionalFormula);
        }
    }
}
