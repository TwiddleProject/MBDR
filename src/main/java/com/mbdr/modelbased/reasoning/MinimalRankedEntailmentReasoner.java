package com.mbdr.modelbased.reasoning;

import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.exceptions.MissingRankConstructor;
import com.mbdr.common.exceptions.MissingRanking;
import com.mbdr.common.services.DefeasibleReasoner;
import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.modelbased.structures.RankedInterpretation;
import com.mbdr.utils.parsing.Parsing;

/**
 * This class provides the foundation for all model-based defeasible reasoners that make use of minimal ranked
 * entailment for world-based representations
 */
public class MinimalRankedEntailmentReasoner implements DefeasibleReasoner {

    // Private reference to a RankConstructor that produces RankedInterpretations
    private RankConstructor<RankedInterpretation> constructor;
    // Private RankedInterpretation model of a given knowledge base - used to answer entailment queries
    private RankedInterpretation model;

    /**
     * Constructor to produce reasoner with a pre-existing RankedInterpretation model
     * @param model
     */
    public MinimalRankedEntailmentReasoner(RankedInterpretation model) {
        this.model = model;
    }

    /**
     * Constructor to produce reasoner with a RankConstructor capable of generating the necessary RankedInterpretation
     * model needed for reasoning
     * @param constructor - RankConstructor<RankedInterpretation>
     */
    public MinimalRankedEntailmentReasoner(RankConstructor<RankedInterpretation> constructor) {
        this.constructor = constructor;
    }

    /**
     * Setter method to set the RankedInterpretation model used for reasoning
     * @param model - RankedInterpretation
     */
    public void setModel(RankedInterpretation model) {
        this.model = model;
    }

    /**
     * Setter method to set the RankConstructor used for generating RankedInterpretation models
     * @param constructor - RankConstructor<RankedInterpretation>
     */
    public void setModelConstructor(RankConstructor<RankedInterpretation> constructor) {
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
     * Method to answer a given defeasible query using the RankedInterpretation model.
     * This method essentially checks whether the ranked model satisfies the given defeasible query.
     * @param defeasibleFormula - materialised defeasible query
     * @return - true or false
     */
    private boolean checkMinimalWorlds(Implication defeasibleFormula) {
        boolean foundMinRank = false;
        for (int i = 0; i < this.model.getRankCount(); ++i) {
            for (NicePossibleWorld world : this.model.getRank(i)) {
                if (world.satisfies(defeasibleFormula.getFirstFormula())) {
                    foundMinRank = true;
                    if (!world.satisfies(defeasibleFormula.getSecondFormula())) {
                        return false;
                    }
                }
            }
            if (foundMinRank) {
                return true;
            }
        }
        return true;
    }

    /**
     * Method to answer a given propositional query using the RankedInterpretation model
     * @param propositionalFormula - propositional query
     * @return - true or false
     */
    private boolean checkAllWorlds(PlFormula propositionalFormula) {
        for (int i = 0; i < this.model.getRankCount(); ++i) {
            for (NicePossibleWorld world : this.model.getRank(i)) {
                if (!world.satisfies(propositionalFormula)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Method to answer a given defeasible query using the RankedInterpretation model
     * @param defeasibleImplication - materialised defeasible query
     * @return true or false
     */
    @Override
    public boolean queryDefeasible(Implication defeasibleImplication) {
        if (this.model == null)
            throw new MissingRanking("Ranked model has not been constructed.");
        return checkMinimalWorlds(defeasibleImplication);
    }

    /**
     * Method to answer a purely propositional query using the RankedInterpretation model
     * @param formula - propositional query
     * @return true or false
     */
    @Override
    public boolean queryPropositional(PlFormula formula) {
        if (this.model == null)
            throw new MissingRanking("Base rank has not been constructed.");
        return queryDefeasible(Parsing.normalizePropositionalFormula(formula));
    }

}
