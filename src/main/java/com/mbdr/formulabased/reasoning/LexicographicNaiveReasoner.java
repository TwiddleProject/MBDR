package com.mbdr.formulabased.reasoning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.reasoner.SatReasoner;
import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;

import com.mbdr.common.exceptions.MissingRankConstructor;
import com.mbdr.common.exceptions.MissingRanking;
import com.mbdr.common.services.DefeasibleReasoner;
import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.formulabased.Utils;
import com.mbdr.utils.parsing.Parser;

public class LexicographicNaiveReasoner implements DefeasibleReasoner{

    //TODO Create class for this
    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;

    public LexicographicNaiveReasoner(ArrayList<PlBeliefSet> baseRank){
        this.baseRank = new ArrayList<>(baseRank);
    }

    public LexicographicNaiveReasoner(RankConstructor<ArrayList<PlBeliefSet>> constructor){
        this.constructor = constructor;
    }

    @Override
    public void build(DefeasibleKnowledgeBase knowledge){
        if(this.constructor == null) throw new MissingRankConstructor("Cannot build base rank without RankConstructor");
        this.baseRank = constructor.construct(knowledge);
    }

    @Override
    public boolean queryDefeasible(Implication formula){
        if(this.baseRank == null) throw new MissingRanking("Base rank of formulas has not been constructed.");
        SatReasoner classicalReasoner = new SatReasoner();
        PlParser parser = new PlParser();
        SatSolver.setDefaultSolver(new Sat4jSolver());
        while (this.baseRank.size() > 1) {
            // System.out.println("We are checking whether or not "
            // + (new Negation(((Implication) formula).getFormulas().getFirst())).toString()
            // + " is entailed by: "
            // + combine(this.baseRank).toString());
            if (classicalReasoner.query(Utils.combine(this.baseRank),
                    new Negation(formula.getFormulas().getFirst()))) {
                if (this.baseRank.get(0).size() > 1) {
                    // System.out.println("It does!");
                    // System.out.println(
                    // "Each possible refinement's first formula is subject to removal, and at each
                    // check each subsequent formula is removed until no more removals can be made.
                    // The refinements contain:"
                    // + this.baseRank.get(0).toString());
                    Object[] c = this.baseRank.get(0).toArray();
                    ArrayList<ArrayList<Object>> rankSet = new ArrayList<>();

                    for (int i = 0; i < c.length; i++) {
                        ArrayList<Object> currRank = new ArrayList<>(Arrays.asList(c));
                        rankSet.add(currRank);
                    }
                    // Checking every subset in a way described in the paper.
                    while (rankSet.get(0).size() > 1) {
                        for (int i = 0; i < rankSet.size(); i++) {

                            ArrayList<Object> temp = rankSet.get(i);
                            if (i > temp.size() - 1) {
                                temp.remove(0);
                            } else {
                                temp.remove(i);
                            }

                            rankSet.set(i, temp);

                            PlBeliefSet tempSet = new PlBeliefSet();
                            for (Object o : temp) {
                                try{
                                    PlFormula tempFormula = (PlFormula) parser.parseFormula(o.toString());
                                    tempSet.add(tempFormula);
                                }
                                catch(IOException ioe){
                                    throw new InvalidFormula("Encountered invalid formula during entailment.");
                                }
                            }
                            this.baseRank.set(0, tempSet);
                            // System.out.println("tempSet"+tempSet.toString());
                            if (!classicalReasoner.query(Utils.combine(this.baseRank),
                                    new Negation(formula.getFormulas().getFirst()))) {
                                // System.out.println(
                                // (new Negation(((Implication) formula).getFormulas().getFirst())).toString()
                                // + " is not entailed by this refinement.");
                                // System.out.println("We now check whether or not the formula" +
                                // formula.toString()
                                // + " is entailed by " + combine(this.baseRank).toString());
                                if (classicalReasoner.query(Utils.combine(this.baseRank), formula)) {
                                    return true;
                                } else {
                                    return false;
                                }

                            }
                        }
                    }
                    // System.out.println("The remaining statements in our refined ranking do entail
                    // "
                    // + (new Negation(((Implication) formula).getFormulas().getFirst())).toString()
                    // + ". We carry on by removing the top rank.");
                    this.baseRank.remove(0);
                } else {
                    // System.out.println("It does! So we remove the top rank.");
                    this.baseRank.remove(0);
                }
            } else {
                // System.out.println("It does not!");
                // System.out.println("We now check whether or not the formula " +
                // formula.toString() + " is entailed by "
                // + combine(this.baseRank).toString());
                if (classicalReasoner.query(Utils.combine(this.baseRank), formula)) {
                    return true;
                } else {
                    // System.out.println("The formula " + formula.toString() + " is not entailed by
                    // "
                    // + combine(this.baseRank).toString());
                    return false;
                }
            }
        }
        // Since we do not check the refinements of the infinite rank
        // System.out.println("We now check whether or not the formula " +
        // formula.toString() + " is entailed by "
        // + combine(this.baseRank).toString());
        if (classicalReasoner.query(Utils.combine(this.baseRank), formula)) {
            return true;
        } else {
            // System.out.println(
            // "The formula " + formula.toString() + " is not entailed by " +
            // combine(this.baseRank).toString());
            return false;
        }

    }

    @Override
    public boolean queryPropositional(PlFormula formula){
        if(this.baseRank == null) throw new MissingRanking("Base rank of formulas has not been constructed.");
        return queryDefeasible(Parser.normalizePropositionalFormula(formula));
    }

}
