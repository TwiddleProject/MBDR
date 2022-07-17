package com.mbdr.formulabased;

import java.util.*;

import org.tweetyproject.logics.pl.syntax.Contradiction;
import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.services.DefeasibleQueryChecker;
import com.mbdr.services.RankConstructor;
import com.mbdr.structures.DefeasibleKnowledgeBase;
import com.mbdr.utils.exceptions.MissingRankConstructor;
import com.mbdr.utils.exceptions.MissingRanking;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.reasoner.*;

public class RationalDirectChecker implements DefeasibleQueryChecker{

    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;
    private DefeasibleKnowledgeBase knowledge;

    public RationalDirectChecker(RankConstructor<ArrayList<PlBeliefSet>> constructor){
        this.constructor = constructor;
    }

    public RationalDirectChecker(ArrayList<PlBeliefSet> baseRank, DefeasibleKnowledgeBase knowledge){
        this.baseRank = baseRank;
        this.knowledge = knowledge;
    }

    @Override
    public void build(DefeasibleKnowledgeBase knowledge){
        if(this.constructor == null) throw new MissingRankConstructor("Cannot construct base rank without RankConstructor.");
        this.baseRank = this.constructor.construct(knowledge);
        this.knowledge = knowledge;
    }

    /**
     * Standard, unoptimised RationalClosure algorithm implementation
     * 
     * @param KB_C     - Knowledge base containing all the purely classical formulas
     *                 of the given knowledge base
     * @param KB_D     - Knowledge base containing all the DIs of the given
     *                 knowledge base
     * @param defeasibleImplication - query to check
     * @return
     */
    public boolean queryDefeasible(Implication defeasibleImplication) {
        if(this.baseRank == null || this.knowledge == null) throw new MissingRanking("Cannot perform query without both base rank and knowledge base.");
        SatSolver.setDefaultSolver(new Sat4jSolver());
        SatReasoner reasoner = new SatReasoner();
        PlBeliefSet R = new PlBeliefSet(this.knowledge.getDefeasibleKnowledge());
        Negation query_negated_antecedent = new Negation(defeasibleImplication.getFirstFormula());

        int i = 0;
        while (reasoner.query(DefeasibleKnowledgeBase.union(this.knowledge.getPropositionalKnowledge(), R), query_negated_antecedent)
                && !R.isEmpty()) {
            R.removeAll(this.baseRank.get(i));
            i++;
        }

        return reasoner.query(DefeasibleKnowledgeBase.union(knowledge.getPropositionalKnowledge(), R), defeasibleImplication);
    }

    @Override
    public boolean queryPropositional(PlFormula formula){
        return queryDefeasible(new Implication(new Negation(formula), new Contradiction()));
    }

    // public static boolean RationalClosureDirectImplementation_Benchmarking(ArrayList<PlBeliefSet> ranked_KB,
    //         DefeasibleKnowledgeBase knowledge,
    //         String rawQuery) throws ParserException, IOException {

    //     PlParser parser = new PlParser();

    //     Implication defeasibleImplication = (Implication) parser
    //             .parseFormula(Parser.materialiseDefeasibleImplication(rawQuery));

    //     SatSolver.setDefaultSolver(new Sat4jSolver());
    //     SatReasoner reasoner = new SatReasoner();

    //     PlBeliefSet R = knowledge.getDefeasibleKnowledge();
    //     Negation query_negated_antecedent = new Negation(defeasibleImplication.getFirstFormula());

    //     int i = 0;
    //     while (reasoner.query(DefeasibleKnowledgeBase.union(knowledge.getPropositionalKnowledge(), R), query_negated_antecedent)
    //             && !R.isEmpty()) {
    //         R.removeAll(ranked_KB.get(i));
    //         i++;
    //     }

    //     return reasoner.query(DefeasibleKnowledgeBase.union(knowledge.getPropositionalKnowledge(), R), defeasibleImplication);
    // }

}
