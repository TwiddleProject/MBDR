package com.mbdr.modelbased.reasoning;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.services.DefeasibleReasoner;
import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.modelbased.structures.RankedInterpretation;
import com.mbdr.utils.exceptions.MissingRankConstructor;
import com.mbdr.utils.exceptions.MissingRanking;

public class MinimalRankedEntailmentReasoner implements DefeasibleReasoner{
    
    private RankConstructor<RankedInterpretation> constructor;
    private RankedInterpretation model;

    public MinimalRankedEntailmentReasoner(RankedInterpretation model){
        this.model = model;
    }

    public MinimalRankedEntailmentReasoner(RankConstructor<RankedInterpretation> constructor){
        this.constructor = constructor;
    }

    public void setModel(RankedInterpretation model){
        this.model = model;
    }

    public void setModelConstructor(RankConstructor<RankedInterpretation> constructor){
        this.constructor = constructor;
    }

    @Override
    public void build(DefeasibleKnowledgeBase knowledge){
        if(this.constructor == null) throw new MissingRankConstructor("Cannot build model without a RankConstructor.");
        this.model = constructor.construct(knowledge);
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
    public boolean queryDefeasible(Implication defeasibleImplication){
        if(this.model == null) throw new MissingRanking("Ranked model has not been constructed.");
        return checkMinimalWorlds(defeasibleImplication);
    }

    @Override
    public boolean queryPropositional(PlFormula formula){
        if(this.model == null) throw new MissingRanking("Ranked model has not been constructed.");
        return checkAllWorlds(formula);
    }

}
