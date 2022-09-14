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
import com.mbdr.formulabased.construction.BaseRank;
import com.mbdr.utils.parsing.Parsing;

/**
 * Basis lexicographic algorithm.
 * Implementation from SCADR project (2021).
 */
public class LexicographicNaiveReasoner implements DefeasibleReasoner{

    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;

    /**
     * Default constructor
     */
    public LexicographicNaiveReasoner(){
        this(new BaseRank());
    }

    /**
     * Parameterized constructor
     * 
     * @param baseRank The base rank of the knowledge
     */
    public LexicographicNaiveReasoner(ArrayList<PlBeliefSet> baseRank){
        this.baseRank = new ArrayList<>(baseRank);
    }

    /**
     * Parameterized constructor
     * 
     * @param constructor The base rank constructor
     */
    public LexicographicNaiveReasoner(RankConstructor<ArrayList<PlBeliefSet>> constructor){
        this.constructor = constructor;
    }

    /**
     * Build reasoner backend (base rank)
     * 
     * @param knowledge The knowledge base
     */
    @Override
    public void build(DefeasibleKnowledgeBase knowledge){
        if(this.constructor == null) throw new MissingRankConstructor("Cannot build base rank without RankConstructor");
        this.baseRank = constructor.construct(knowledge);
    }

    /**
     * Query a defeasible implication
     * 
     * @param formula The defeasible implication
     * @return Whether the query is entailed
     */
    @Override
    public boolean queryDefeasible(Implication formula){
        if(this.baseRank == null) throw new MissingRanking("Base rank of formulas has not been constructed.");
        SatReasoner classicalReasoner = new SatReasoner();
        PlParser parser = new PlParser();
        SatSolver.setDefaultSolver(new Sat4jSolver());
        while (this.baseRank.size() > 1) {
            if (classicalReasoner.query(Utils.combine(this.baseRank),
                    new Negation(formula.getFormulas().getFirst()))) {
                if (this.baseRank.get(0).size() > 1) {
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
                            if (!classicalReasoner.query(Utils.combine(this.baseRank),
                                    new Negation(formula.getFormulas().getFirst()))) {
                                if (classicalReasoner.query(Utils.combine(this.baseRank), formula)) {
                                    return true;
                                } else {
                                    return false;
                                }

                            }
                        }
                    }
                    this.baseRank.remove(0);
                } else {
                    this.baseRank.remove(0);
                }
            } else {
                if (classicalReasoner.query(Utils.combine(this.baseRank), formula)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        if (classicalReasoner.query(Utils.combine(this.baseRank), formula)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Query a propositional formula
     * 
     * @param formula The formula to query
     * @return Whether the query is entailed
     */
    @Override
    public boolean queryPropositional(PlFormula formula){
        if(this.baseRank == null) throw new MissingRanking("Base rank has not been constructed.");
        return queryDefeasible(Parsing.normalizePropositionalFormula(formula));
    }

}
