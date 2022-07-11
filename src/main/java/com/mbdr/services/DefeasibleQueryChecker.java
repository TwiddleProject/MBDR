package com.mbdr.services;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.PlFormula;

public interface DefeasibleQueryChecker {
    
    public boolean query(String formula);
    public boolean queryPropositional(PlFormula formula);
    public boolean queryDefeasible(Implication defeasibleImplication);

}
