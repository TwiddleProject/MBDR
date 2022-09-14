package com.mbdr.modelbased.construction;

import java.util.ArrayList;
import java.util.Arrays;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.syntax.Conjunction;
import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.syntax.Tautology;

import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.modelbased.structures.RankedFormulasInterpretation;

public class FormulaRank implements RankConstructor<RankedFormulasInterpretation> {
    /**
     * Constructs the ranked formula interpretation for a given knowledge base.
     * @param knowledge - Defeasible knowledge base
     * @return RankedFormulasInterpretation
     */
    @Override
    public RankedFormulasInterpretation construct(DefeasibleKnowledgeBase knowledge) {

        RankedFormulasInterpretation rankedModel = new RankedFormulasInterpretation(0);

        Sat4jSolver solver = new Sat4jSolver();

        PlFormula classicalKnowledgeFormula = new Conjunction(knowledge.getPropositionalKnowledge());

        PlBeliefSet remainingDefeasibleFormulas = knowledge.getDefeasibleKnowledge();

        PlFormula F0Defeasible = new Conjunction(knowledge.getDefeasibleKnowledge());

        PlFormula currentRankFormula = new Conjunction(classicalKnowledgeFormula, F0Defeasible);

        // Check whether the materialised knowledge base is consistent
        if (solver.isSatisfiable(knowledge.union())) {

            int rankIndex = rankedModel.addRank(currentRankFormula);

            int numPreviousRemainingDefeasibleFormulas = -1;

            while (!remainingDefeasibleFormulas.isEmpty()
                    && numPreviousRemainingDefeasibleFormulas != remainingDefeasibleFormulas.size()) {
                // Find the defeasible formulas whose antecedents are consistent with the
                // current rank formula
                PlBeliefSet checkedFormulas = new PlBeliefSet();
                for (PlFormula formula : remainingDefeasibleFormulas) {
                    PlFormula antecedent = ((Implication) formula).getFirstFormula();

                    if (solver.isSatisfiable(Arrays.asList(currentRankFormula, antecedent))) {
                        checkedFormulas.add(formula);
                    }

                }

                numPreviousRemainingDefeasibleFormulas = remainingDefeasibleFormulas.size();
                remainingDefeasibleFormulas.removeAll(checkedFormulas);

                PlFormula remainingDefeasibleConjunction = new Conjunction(remainingDefeasibleFormulas);

                ArrayList<PlFormula> negatedPreviousRanks = new ArrayList<>();

                for (int i = 0; i <= rankIndex; i++) {
                    negatedPreviousRanks.add(new Negation(rankedModel.getRank(i)));
                }

                PlFormula negatedPreviousRanksConjunction = new Conjunction(negatedPreviousRanks);

                currentRankFormula = new Conjunction(Arrays.asList(
                        classicalKnowledgeFormula,
                        negatedPreviousRanksConjunction,
                        remainingDefeasibleConjunction));

                rankIndex = rankedModel.addRank(currentRankFormula);
            }

            if (remainingDefeasibleFormulas.isEmpty()) {
                rankedModel.setInfiniteRank(new Negation(classicalKnowledgeFormula));
            } else {
                rankedModel.setInfiniteRank(new Negation(rankedModel.getRank(rankIndex)));
            }

        } else {
            // If the materialised knowledge is inconsistent then place every world on infinite rank
            rankedModel.setInfiniteRank(new Tautology());
        }

        return rankedModel;
    }
}
