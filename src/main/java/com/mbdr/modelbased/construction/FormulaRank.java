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

    @Override
    public RankedFormulasInterpretation construct(DefeasibleKnowledgeBase knowledge) {

        RankedFormulasInterpretation rankedModel = new RankedFormulasInterpretation(0);

        // System.out.println("empty rankedModel:\n" + rankedModel);

        // System.out.println("----------------------------------");

        Sat4jSolver solver = new Sat4jSolver();

        PlFormula classicalKnowledgeFormula = new Conjunction(knowledge.getPropositionalKnowledge());

        PlBeliefSet remainingDefeasibleFormulas = knowledge.getDefeasibleKnowledge();

        PlFormula F0Defeasible = new Conjunction(knowledge.getDefeasibleKnowledge());

        PlFormula currentRankFormula = new Conjunction(classicalKnowledgeFormula, F0Defeasible);

        // System.out.println("F0:\t" + currentRankFormula);

        if (solver.isSatisfiable(knowledge.union())) {
            // System.out.println("----------------------------------");
            // System.out.println("Knowledge is consistent.");
            // System.out.println("----------------------------------");

            int rankIndex = rankedModel.addRank(currentRankFormula);
            // System.out.println("Added rank " + rankIndex + ":\t" + currentRankFormula);

            // System.out.println("rankedModel:\n" + rankedModel);

            int numPreviousRemainingDefeasibleFormulas = -1;

            // System.out.println("remainingDefeasibleFormulas:\t" +
            // remainingDefeasibleFormulas);

            while (!remainingDefeasibleFormulas.isEmpty()
                    && numPreviousRemainingDefeasibleFormulas != remainingDefeasibleFormulas.size()) {
                // System.out.println("Start while loop");
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
                // System.out.println("remainingDefeasibleFormulas:\t" +
                // remainingDefeasibleFormulas);

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
            // System.out.println("----------------------------------");
            // System.out.println("Knowledge is inconsistent.");
            // System.out.println("----------------------------------");
            rankedModel.setInfiniteRank(new Tautology());
        }

        return rankedModel;
    }
}
