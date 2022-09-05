package com.mbdr.modelbased.reasoning;

import java.util.Arrays;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.services.DefeasibleReasoner;
import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.modelbased.structures.RankedFormulasInterpretation;
import com.mbdr.utils.parsing.Parsing;
import com.mbdr.common.exceptions.MissingRankConstructor;
import com.mbdr.common.exceptions.MissingRanking;

public class MinimalRankedEntailmentFormulaReasoner implements DefeasibleReasoner {

    private RankConstructor<RankedFormulasInterpretation> constructor;
    private RankedFormulasInterpretation model;

    public MinimalRankedEntailmentFormulaReasoner(RankedFormulasInterpretation model) {
        this.model = model;
    }

    public MinimalRankedEntailmentFormulaReasoner(RankConstructor<RankedFormulasInterpretation> constructor) {
        this.constructor = constructor;
    }

    public void setModel(RankedFormulasInterpretation model) {
        this.model = model;
    }

    public void setModelConstructor(RankConstructor<RankedFormulasInterpretation> constructor) {
        this.constructor = constructor;
    }

    @Override
    public void build(DefeasibleKnowledgeBase knowledge) {
        if (this.constructor == null)
            throw new MissingRankConstructor("Cannot build model without a RankConstructor.");
        this.model = constructor.construct(knowledge);
    }

    private boolean checkMinimalWorlds(Implication defeasibleFormula) {
        Sat4jSolver solver = new Sat4jSolver();

        for (int i = 0; i < this.model.getRankCount(); ++i) {
            PlFormula currentRankFormula = this.model.getRank(i);
            PlFormula antecedent = defeasibleFormula.getFirstFormula();

            PlBeliefSet currentRankFormulaAndAntecedent = new PlBeliefSet();
            currentRankFormulaAndAntecedent.add(currentRankFormula, antecedent);

            if (solver.isConsistent(Arrays.asList(currentRankFormula, antecedent))) {
                // i.e. "found minimal world" -> now check whether consequent is entailed

                return !solver.isConsistent(Arrays.asList(currentRankFormula, antecedent,
                        new Negation(defeasibleFormula.getSecondFormula())));

            }
        }

        return true;
    }

    private boolean checkAllWorlds(PlFormula propositionalFormula) {
        Sat4jSolver solver = new Sat4jSolver();

        for (int i = 0; i < this.model.getRankCount(); ++i) {
            PlFormula currentRankFormula = this.model.getRank(i);

            if (solver.isSatisfiable(Arrays.asList(currentRankFormula, new Negation(propositionalFormula)))) {
                return false;
            }

        }
        return true;
    }

    @Override
    public boolean queryPropositional(PlFormula formula) {
        if (this.model == null)
            throw new MissingRanking("Base rank has not been constructed.");
        return queryDefeasible(Parsing.normalizePropositionalFormula(formula));
    }

    @Override
    public boolean queryDefeasible(Implication defeasibleImplication) {
        if (this.model == null)
            throw new MissingRanking("Ranked model has not been constructed.");
        return checkMinimalWorlds(defeasibleImplication);
    }

}
