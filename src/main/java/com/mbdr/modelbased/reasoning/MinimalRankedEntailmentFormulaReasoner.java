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
/**
 * This class provides the foundation for all model-based defeasible reasoners that make use of minimal ranked
 * entailment for formula-based representations
 */
public class MinimalRankedEntailmentFormulaReasoner implements DefeasibleReasoner {

    // Private reference to a RankConstructor that produces RankedFormulasInterpretations
    private RankConstructor<RankedFormulasInterpretation> constructor;
    // Private RankedFormulasInterpretation model of a given knowledge base - used to answer entailment queries
    private RankedFormulasInterpretation model;

    /**
     * Constructor to produce reasoner with a pre-existing RankedFormulasInterpretation model
     * @param model
     */
    public MinimalRankedEntailmentFormulaReasoner(RankedFormulasInterpretation model) {
        this.model = model;
    }

    /**
     * Constructor to produce reasoner with a RankConstructor capable of generating the necessary RankedFormulasInterpretation
     * model needed for reasoning
     * @param constructor - RankConstructor<RankedFormulasInterpretation>
     */
    public MinimalRankedEntailmentFormulaReasoner(RankConstructor<RankedFormulasInterpretation> constructor) {
        this.constructor = constructor;
    }

    /**
     * Setter method to set the RankedInterpretation model used for reasoning
     * @param model - RankedFormulasInterpretation
     */
    public void setModel(RankedFormulasInterpretation model) {
        this.model = model;
    }

    /**
     * Setter method to set the RankConstructor used for generating RankedFormulasInterpretation models
     * @param constructor - RankConstructor<RankedFormulasInterpretation>
     */
    public void setModelConstructor(RankConstructor<RankedFormulasInterpretation> constructor) {
        this.constructor = constructor;
    }

    /**
     * Method that uses the built-in reference to a RankConstructor, if it exists, to build a ranked model of a given
     * knowledge base
     * @param knowledge - DefeasibleKnowledgeBase
     */
    @Override
    public void build(DefeasibleKnowledgeBase knowledge) {
        if (this.constructor == null)
            throw new MissingRankConstructor("Cannot build model without a RankConstructor.");
        this.model = constructor.construct(knowledge);
    }

    /**
     * Method to answer a given defeasible query using the RankedFormulasInterpretation model.
     * This method essentially checks whether the ranked model, using representative formulas instead of worlds,
     * satisfies the given defeasible query.
     * @param defeasibleFormula - materialised defeasible query
     * @return - true or false
     */
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

    /**
     * Method to answer a given propositional query using the RankedFormulasInterpretation model
     * @param propositionalFormula - propositional query
     * @return - true or false
     */
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

    /**
     * Method to answer a purely propositional query using the RankedFormulasInterpretation model
     * @param formula - propositional query
     * @return true or false
     */
    @Override
    public boolean queryPropositional(PlFormula formula) {
        if (this.model == null)
            throw new MissingRanking("Base rank has not been constructed.");
        return queryDefeasible(Parsing.normalizePropositionalFormula(formula));
    }

    /**
     * Method to answer a given defeasible query using the RankedFormulasInterpretation model
     * @param defeasibleImplication - materialised defeasible query
     * @return true or false
     */
    @Override
    public boolean queryDefeasible(Implication defeasibleImplication) {
        if (this.model == null)
            throw new MissingRanking("Ranked model has not been constructed.");
        return checkMinimalWorlds(defeasibleImplication);
    }

}
