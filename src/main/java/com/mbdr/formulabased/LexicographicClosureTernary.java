package com.mbdr.formulabased;

import org.tweetyproject.logics.pl.syntax.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.logics.pl.reasoner.*;
import java.io.IOException;


import org.tweetyproject.commons.ParserException;

import java.util.*;

public class LexicographicClosureTernary {

    static SatReasoner classicalReasoner = new SatReasoner();

    static int rankFromWhichToRemove = -1;
    static int counter = 0;
    static int counterR = 0;
    static Boolean checkEntailmentTernarySearch(PlBeliefSet[] rKB, PlFormula formula, int left, int right)
            throws ParserException, IOException {
        PlFormula negationOfAntecedent = new Negation(((Implication) formula).getFormulas().getFirst());
        SatSolver.setDefaultSolver(new Sat4jSolver());
        PlBeliefSet[] rankedKB = rKB.clone();

        if (right > left) {
            //Setting up the key values
            int mid1 = left + ((right - left) / 3);
            int mid2 = right - ((right - left) / 3);
            counter++;
            //If it is the bottom 2/3 of the ranks
            if (classicalReasoner.query(combine(Arrays.copyOfRange(rankedKB, mid1 + 1, rankedKB.length)),
                    negationOfAntecedent)) {
                        
                if (mid2 < rankedKB.length) {
                    counter++;
                    //If it is the bottom 1/3 of the ranks
                    if (classicalReasoner.query(combine(Arrays.copyOfRange(rankedKB, mid2 + 1, rankedKB.length)),
                            negationOfAntecedent)) {                          
                                
      
                        return checkEntailmentTernarySearch(rankedKB, formula, mid2 + 1, right);
                    } else {
                        counter++;
                        //Checking whether adding back one rank changes the compatibility of the query
                        if (classicalReasoner.query(combine(Arrays.copyOfRange(rankedKB, mid2, rankedKB.length)),
                                negationOfAntecedent)) {
                            rankFromWhichToRemove = mid2;
                        } else {
                            return checkEntailmentTernarySearch(rankedKB, formula, mid1 + 1, mid2 - 1);
                        }
                    }
                } else if (mid2 == rankedKB.length) {
                    return checkEntailmentTernarySearch(rankedKB, formula, mid1 + 1, mid2 - 1);

                }
            } else {
                counter++;
                //Checking whether adding back one rank changes the compatibility of the query
                if (classicalReasoner.query(combine(Arrays.copyOfRange(rankedKB, mid1, rankedKB.length)),
                        negationOfAntecedent)) {
                          
                    rankFromWhichToRemove = mid1;
                } else {
                    return checkEntailmentTernarySearch(rankedKB, formula, left, mid1);
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
    
            if (classicalReasoner.query(combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)),
                    formula)) {
                        
                return true;
            } else
                return false;
        }
        else if (rankFromWhichToRemove +1< rankedKB.length) {
     
            Object[] rank = rankedKB[rankFromWhichToRemove].toArray();
          
            // LexicographicClosurePowerset p = new LexicographicClosurePowerset();

            List<Set<Object>> sortedRank = LexicographicClosurePowerset.sortList(rank);

            ArrayList<String> refinements = new ArrayList<>(LexicographicClosurePowerset.combineRefine(sortedRank));
            for (String f : refinements) {
                PlBeliefSet combSet = new PlBeliefSet();
                PlParser parser = new PlParser();
                combSet.add((PlFormula) parser.parseFormula(f));
                rankedKB[rankFromWhichToRemove] = combSet;
              
                counterR++;
                if (!classicalReasoner.query(
                        combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)),
                        new Negation(((Implication) formula).getFormulas().getFirst()))) {
                           
               //     System.out.println((new Negation(((Implication) formula).getFormulas().getFirst())).toString()
                 //           + " is not entailed by this refinement.");
                  //  System.out.println("We now check whether or not the formula" + formula.toString()
                  //          + " is entailed by "
                   //         + combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)).toString());
                   counter++;
                    if (classicalReasoner.query(
                            combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)), formula)) {
                                
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
            if (classicalReasoner.query(combine(Arrays.copyOfRange(rankedKB, rankFromWhichToRemove, rankedKB.length)),
                    formula)) {
                return true;
            } else
                return false;
        } else {
            return false;
        }

    }

    static PlBeliefSet combine(PlBeliefSet[] ranks) {
        PlBeliefSet combined = new PlBeliefSet();
        for (PlBeliefSet rank : ranks) {
            combined.addAll(rank);
        }
        return combined;
    }

}
