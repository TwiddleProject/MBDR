package com.mbdr.formulabased;

import org.tweetyproject.logics.pl.syntax.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;

import com.mbdr.services.DefeasibleQueryChecker;
import com.mbdr.services.RankConstructor;
import com.mbdr.structures.DefeasibleKnowledgeBase;
import com.mbdr.utils.exceptions.MissingRankConstructor;
import com.mbdr.utils.exceptions.MissingRanking;

import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.reasoner.*;

import java.util.*;

public class LexicographicBinaryChecker implements DefeasibleQueryChecker{

    static int counter = 0;
    static int counterR = 0;
    
    private SatReasoner classicalReasoner = new SatReasoner();
    private int rankFromWhichToRemove = -1;
    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;

    public LexicographicBinaryChecker(ArrayList<PlBeliefSet> baseRank){
        this.baseRank = baseRank;
        this.constructor = null;
    }

    public LexicographicBinaryChecker(RankConstructor<ArrayList<PlBeliefSet>> constructor){
        this.baseRank = null;
        this.constructor = constructor;
    }

    @Override
    public void build(DefeasibleKnowledgeBase knowledge){
        if(this.constructor == null) throw new MissingRankConstructor("Cannot build Base Rank without a RankConstructor.");
        this.baseRank = this.constructor.construct(knowledge);
    }

    @Override
    public boolean queryDefeasible(Implication defeasibleImplication){
        if(this.baseRank == null) throw new MissingRanking("Base rank of formulas has not been constructed.");
        PlBeliefSet[] baseRankCopy = new PlBeliefSet[this.baseRank.size()]; 
        baseRankCopy = this.baseRank.toArray(baseRankCopy);
        return this.queryDefeasibleBinary(baseRankCopy, defeasibleImplication, 0, baseRankCopy.length);
    }

    @Override
    public boolean queryPropositional(PlFormula formula){
        if(this.baseRank == null) throw new MissingRanking("Base rank of formulas has not been constructed.");
        return queryDefeasible(new Implication(new Negation(formula), new Contradiction()));
    }

    private boolean queryDefeasibleBinary(PlBeliefSet[] rKB, Implication formula, int left, int right){
        PlFormula negationOfAntecedent = new Negation(formula.getFormulas().getFirst());
        SatSolver.setDefaultSolver(new Sat4jSolver());
        PlBeliefSet[] rankedKB = rKB.clone();
        if (right > left) {
            
            int mid = left + ((right - left) / 2);
            // If the query is still compatible after removing middle one and the ones above it, remove the top half
            counter++;
            if (classicalReasoner.query(Utils.combine(Arrays.copyOfRange(rankedKB, mid + 1, rankedKB.length)),
                    negationOfAntecedent)) {
                      
 
                return queryDefeasibleBinary(rankedKB, formula, mid + 1, right);
            }
            // Since the query is not compatible after removing the top half, check if adding in one rank back makes the query compatible
            else {
                counter++;
                if (classicalReasoner.query(Utils.combine(Arrays.copyOfRange(rankedKB, mid, rankedKB.length)),
                        negationOfAntecedent)) {
                            

                    rankFromWhichToRemove = mid;
                } else { // removing it still means the query is compatible. The corresponding rank is in the bottom half.

                    return queryDefeasibleBinary(rankedKB, formula, left, mid);
                }
            }
        } 
        else {
            if (right == left)
                rankFromWhichToRemove = right;
            else
                return false;

        }
        if (rankFromWhichToRemove == 0){
            counter++;

            if (classicalReasoner.query(Utils.combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)),
                    formula)) {
                     
                return true;
            } else
                return false;
        }
        else if (rankFromWhichToRemove +1 < rankedKB.length) {

            Object[] rank = rankedKB[rankFromWhichToRemove].toArray();
            
            List<Set<Object>> sortedRank = LexicographicPowersetChecker.sortList(rank);

            ArrayList<String> refinements = new ArrayList<>(LexicographicPowersetChecker.combineRefine(sortedRank)); // Calling the powerset function
            for (String f : refinements) { // Checking every subsets
                PlBeliefSet combSet = new PlBeliefSet();
                PlParser parser = new PlParser();
                try {
                    combSet.add((PlFormula) parser.parseFormula(f));
                } catch (Exception e) {
                    throw new InvalidFormula("Unexpected formula encountered during entailment.");
                }
                
                rankedKB[rankFromWhichToRemove] = combSet;
                counterR++;
                //System.out.println("ranked kb" + rankedKB[rankFromWhichToRemove].toString());
                if (!classicalReasoner.query(Utils.combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)),
                        new Negation(((Implication) formula).getFormulas().getFirst()))) {
                          
                 //   System.out.println((new Negation(((Implication) formula).getFormulas().getFirst())).toString()
                 //           + " is not entailed by this refinement.");
                 //   System.out.println("We now check whether or not the formula" + formula.toString()
                  //          + " is entailed by " + combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)).toString());
                  counter++;
                    if (classicalReasoner.query(
                        Utils.combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)), formula)) {
                           
                        return true;
                    } else {
                        return false;
                    }

                } else {
                    return false;
                }
            }
            return true;
        
    }
        //Since we do not check the refinements of the infinite rank
    else if (rankFromWhichToRemove +1 == rankedKB.length){
       // System.out.println("We now check whether or not the formula" + formula.toString()
               //     + " is entailed by "
                  //  + combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)).toString());
                  counter++;
        if (classicalReasoner.query(
            Utils.combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)), formula)){
 
                return true;
            }
            else return false;
    }
    else{
        return false;
    }
        
    }

    

}