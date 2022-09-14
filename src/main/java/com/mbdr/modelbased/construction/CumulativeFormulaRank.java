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

public class CumulativeFormulaRank implements RankConstructor<RankedFormulasInterpretation> {
    /**
     * Constructs the cumulative ranked formula interpretation for a given knowledge base.
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

        if (solver.isSatisfiable(knowledge.union())) {

            int rankIndex = rankedModel.addRank(currentRankFormula);

            int numPreviousRemainingDefeasibleFormulas = -1;

            while (!remainingDefeasibleFormulas.isEmpty()
                    && numPreviousRemainingDefeasibleFormulas != remainingDefeasibleFormulas.size()) {

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

                currentRankFormula = new Conjunction(Arrays.asList(
                        classicalKnowledgeFormula,
                        remainingDefeasibleConjunction));

                rankIndex = rankedModel.addRank(currentRankFormula);
            }

            rankedModel.setInfiniteRank(new Tautology());

        } else {
            rankedModel.setInfiniteRank(new Tautology());
        }

        return rankedModel;
    }
}
