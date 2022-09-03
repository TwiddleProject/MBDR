package com.mbdr.formulabased.reasoning;

import org.tweetyproject.logics.pl.syntax.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;

import com.mbdr.common.exceptions.MissingRankConstructor;
import com.mbdr.common.exceptions.MissingRanking;
import com.mbdr.common.services.DefeasibleReasoner;
import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.formulabased.Utils;
import com.mbdr.formulabased.construction.BaseRank;

import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.reasoner.*;

import java.util.*;

public class LexicographicTernaryReasoner implements DefeasibleReasoner{

    private static SatReasoner classicalReasoner = new SatReasoner();
    private static int rankFromWhichToRemove = -1;
    static int counter = 0;
    static int counterR = 0;

    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;

    public LexicographicTernaryReasoner(){
        this(new BaseRank());
    }

    public LexicographicTernaryReasoner(ArrayList<PlBeliefSet> baseRank){
        this.baseRank = baseRank;
        this.constructor = null;
    }

    public LexicographicTernaryReasoner(RankConstructor<ArrayList<PlBeliefSet>> constructor){
        this.baseRank = null;
        this.constructor = constructor;
    }

    @Override
    public void build(DefeasibleKnowledgeBase knowledge){
        if(this.constructor == null) throw new MissingRankConstructor("Cannot build base rank without RankConstructor");
        this.baseRank = constructor.construct(knowledge);
    }

    @Override
    public boolean queryDefeasible(Implication defeasibleImplication){
        if(this.baseRank == null) throw new MissingRanking("Base rank of formulas has not been constructed.");
        PlBeliefSet[] baseRankCopy = new PlBeliefSet[this.baseRank.size()]; 
        baseRankCopy = this.baseRank.toArray(baseRankCopy);
        return this.queryDefeasibleTernary(baseRankCopy, defeasibleImplication, 0, baseRankCopy.length);
    }

    @Override
    public boolean queryPropositional(PlFormula formula){
        if(this.baseRank == null) throw new MissingRanking("Base rank of formulas has not been constructed.");
        return queryDefeasible(new Implication(new Negation(formula), new Contradiction()));
    }

    private boolean queryDefeasibleTernary(PlBeliefSet[] rKB, Implication implication, int left, int right){
        PlFormula negationOfAntecedent = new Negation(implication.getFormulas().getFirst());
        SatSolver.setDefaultSolver(new Sat4jSolver());
        PlBeliefSet[] rankedKB = rKB.clone();

        if (right > left) {
            //Setting up the key values
            int mid1 = left + ((right - left) / 3);
            int mid2 = right - ((right - left) / 3);
            counter++;
            //If it is the bottom 2/3 of the ranks
            if (classicalReasoner.query(Utils.combine(Arrays.copyOfRange(rankedKB, mid1 + 1, rankedKB.length)),
                    negationOfAntecedent)) {
                        
                if (mid2 < rankedKB.length) {
                    counter++;
                    //If it is the bottom 1/3 of the ranks
                    if (classicalReasoner.query(Utils.combine(Arrays.copyOfRange(rankedKB, mid2 + 1, rankedKB.length)),
                            negationOfAntecedent)) {                          
                                
      
                        return queryDefeasibleTernary(rankedKB, implication, mid2 + 1, right);
                    } else {
                        counter++;
                        //Checking whether adding back one rank changes the compatibility of the query
                        if (classicalReasoner.query(Utils.combine(Arrays.copyOfRange(rankedKB, mid2, rankedKB.length)),
                                negationOfAntecedent)) {
                            rankFromWhichToRemove = mid2;
                        } else {
                            return queryDefeasibleTernary(rankedKB, implication, mid1 + 1, mid2 - 1);
                        }
                    }
                } else if (mid2 == rankedKB.length) {
                    return queryDefeasibleTernary(rankedKB, implication, mid1 + 1, mid2 - 1);

                }
            } else {
                counter++;
                //Checking whether adding back one rank changes the compatibility of the query
                if (classicalReasoner.query(Utils.combine(Arrays.copyOfRange(rankedKB, mid1, rankedKB.length)),
                        negationOfAntecedent)) {
                          
                    rankFromWhichToRemove = mid1;
                } else {
                    return queryDefeasibleTernary(rankedKB, implication, left, mid1);
                }
            }
        } else {
            if (right == left) {
                rankFromWhichToRemove = right;
            }
            else return false;
        }
        if (rankFromWhichToRemove == 0){
            counter++;
    
            if (classicalReasoner.query(Utils.combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)),
            implication)) {
                        
                return true;
            } else
                return false;
        }
        else if (rankFromWhichToRemove +1< rankedKB.length) {
     
            Object[] rank = rankedKB[rankFromWhichToRemove].toArray();
          
            // LexicographicClosurePowerset p = new LexicographicClosurePowerset();

            List<Set<Object>> sortedRank = LexicographicPowersetReasoner.sortList(rank);

            ArrayList<String> refinements = new ArrayList<>(LexicographicPowersetReasoner.combineRefine(sortedRank));
            for (String f : refinements) {
                PlBeliefSet combSet = new PlBeliefSet();
                PlParser parser = new PlParser();
                try {
                    combSet.add((PlFormula) parser.parseFormula(f));
                } catch (Exception e) {
                    throw new InvalidFormula("Unexpected formula encountered during entailment.");
                }
                
                rankedKB[rankFromWhichToRemove] = combSet;
              
                counterR++;
                if (!classicalReasoner.query(
                        Utils.combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)),
                        new Negation(implication.getFormulas().getFirst()))) {
                           
               //     System.out.println((new Negation(((Implication) formula).getFormulas().getFirst())).toString()
                 //           + " is not entailed by this refinement.");
                  //  System.out.println("We now check whether or not the formula" + formula.toString()
                  //          + " is entailed by "
                   //         + combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)).toString());
                   counter++;
                    if (classicalReasoner.query(
                            Utils.combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)), implication)) {
                                
                        return true;
                    } else {
                        return false;
                    }

                } else {
                    return false;
                }
            }
            return true;
            //Since we do not check the refinements of the infinite rank
        } else if (rankFromWhichToRemove + 1 == rankedKB.length) {

         //   System.out.println("There would then be no ranks remaining other than the infinite rank.");
          //  System.out.println("We now check whether or not the formula " + formula.toString() + " is entailed by "
             //       + combine(rankedKB).toString());
   
            counter++;
            if (classicalReasoner.query(Utils.combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)),
                    implication)) {
                return true;
            } else
                return false;
        } else {
            return false;
        }

    }

}
