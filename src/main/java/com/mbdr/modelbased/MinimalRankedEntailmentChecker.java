package com.mbdr.modelbased;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.utils.parsing.Parser;
import com.mbdr.structures.RankedInterpretation;
import com.mbdr.services.DefeasibleQueryChecker;

public class MinimalRankedEntailmentChecker implements DefeasibleQueryChecker{
    
    private RankedInterpretation model;

    public MinimalRankedEntailmentChecker(RankedInterpretation model){
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

    @Override
    public boolean query(String formula){
        if(Parser.isDefeasible(formula)){
            try{
                Implication defeasibleImplication = (Implication)Parser.parseDefeasibleFormula(formula);
                return queryDefeasible(defeasibleImplication);
            } catch(Exception e){
                return false;
            }
        } 
        else {
            try{
                PlFormula propositionalFormula = Parser.parsePropositionalFormula(formula);
                return queryPropositional(propositionalFormula);
            } catch(Exception e){
                return false;
            }
        }
    }

    @Override
    public boolean queryDefeasible(Implication defeasibleImplication){
        return checkMinimalWorlds(defeasibleImplication);
    }

    @Override
    public boolean queryPropositional(PlFormula formula){
        return checkAllWorlds(formula);
    }
}
