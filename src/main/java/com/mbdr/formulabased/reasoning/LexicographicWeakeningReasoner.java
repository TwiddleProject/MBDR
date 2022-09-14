package com.mbdr.formulabased.reasoning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tweetyproject.logics.pl.reasoner.SatReasoner;
import org.tweetyproject.logics.pl.sat.Sat4jSolver;
import org.tweetyproject.logics.pl.sat.SatSolver;
import org.tweetyproject.logics.pl.syntax.Negation;
import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.Conjunction;
import org.tweetyproject.logics.pl.syntax.Disjunction;
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
 * Lexicographic weakening algorithm in Lexicographic Model-based Defeasible Reasoning
 */
public class LexicographicWeakeningReasoner implements DefeasibleReasoner{

    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;

    /**
     * Default constructor
     */
    public LexicographicWeakeningReasoner(){
        this(new BaseRank());
    }

    /**
     * Parameterized constructor
     * 
     * @param baseRank The base rank of the knowledge
     */
    public LexicographicWeakeningReasoner(ArrayList<PlBeliefSet> baseRank){
        this.baseRank = new ArrayList<>(baseRank);
    }

    /**
     * Parameterized constructor
     * 
     * @param constructor The base rank constructor
     */
    public LexicographicWeakeningReasoner(RankConstructor<ArrayList<PlBeliefSet>> constructor){
        this.constructor = constructor;
    }

    /**
     * Build reasoner backend (base rank)
     * 
     * @param knowledge The knowledge base
     */
    @Override
    public void build(DefeasibleKnowledgeBase knowledge){
        if(this.constructor == null) throw new MissingRankConstructor("Cannot build base rank without RankConstructor.");
        this.baseRank = constructor.construct(knowledge);
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

    /**
     * Query a defeasible implication
     * 
     * @param formula The defeasible implication
     * @return Whether the query is entailed
     */
    @Override
    public boolean queryDefeasible(Implication formula){
        if(this.baseRank == null) throw new MissingRanking("Base rank has not been constructed.");
        SatReasoner classicalReasoner = new SatReasoner();
        SatSolver.setDefaultSolver(new Sat4jSolver());
        PlFormula negatedAntecedant = new Negation(formula.getFirstFormula());
        PlBeliefSet weakenedSet = Utils.combine(this.baseRank);
        boolean foundWorld = false;
        for(int rank = 0; rank < this.baseRank.size()-1 && !foundWorld; ++rank){
            if(!classicalReasoner.query(weakenedSet, negatedAntecedant)){
                foundWorld = true;
                break;
            }
            weakenedSet.removeAll(this.baseRank.get(rank));
            List<Set<PlBeliefSet>> rankedSubsets = orderedSubsets(this.baseRank.get(rank));
            for(int size = rankedSubsets.size()-2; size > 0; --size){
                Set<PlBeliefSet> subsets = rankedSubsets.get(size);
                PlFormula weakenedFormula = conjunctionDisjunction(subsets);
                weakenedSet.add(weakenedFormula);
                if(!classicalReasoner.query(weakenedSet, negatedAntecedant)){
                    foundWorld = true;
                    break;
                }
                weakenedSet.remove(weakenedFormula);
            }
        }
        return classicalReasoner.query(weakenedSet, formula);
    }

    /**
     * Creates a formula consisting of the disjunction of the conjuction of elements
     * in each subset
     * 
     * @param subsets The subsets to combine
     * @return The combined formula
     */
    public static PlFormula conjunctionDisjunction(Set<PlBeliefSet> subsets){
        Set<PlFormula> conjunctions = new HashSet<>();
        for(PlBeliefSet subset : subsets){
            conjunctions.add(new Conjunction(subset));
        }
        return new Disjunction(conjunctions);
    }

    /**
     * Generates all subsets of knowledge ordered by size descending
     * 
     * @param defeasibleKnowledge The knowledge
     * @return The ordered subsets
     */
    public static ArrayList<Set<PlBeliefSet>> orderedSubsets(PlBeliefSet defeasibleKnowledge){
        int n = defeasibleKnowledge.size();
        ArrayList<Set<PlBeliefSet>> subsets = new ArrayList<>(n-1);
        HashSet<PlBeliefSet> initial = new HashSet<>();
        initial.add(defeasibleKnowledge);
        // Subset of size n
        subsets.add(initial);
        // For each subset size, descending
        for(int k = n-1; k >= 0; --k){
            Set<PlBeliefSet> currentSubsets = new HashSet<>();
            // For each subset of 1 size larger
            for(PlBeliefSet subset : subsets.get(subsets.size()-1)){
                // Remove each element in turn and add result to current subsets
                for(PlFormula formula : subset){
                    PlBeliefSet current = new PlBeliefSet(subset);
                    current.remove(formula);
                    currentSubsets.add(current);
                }
            }
            // Add currentSubsets to subsets
            subsets.add(currentSubsets);
        }
        return subsets;
    }

    /**
     * Wrapper for ordered subsets of a defeasible knowledge base
     * 
     * @param knowledge The knowledge base
     * @return The ordered subsets of defeasible implications
     */
    public static ArrayList<Set<PlBeliefSet>> orderedSubsets(DefeasibleKnowledgeBase knowledge){
        return orderedSubsets(knowledge.getDefeasibleKnowledge());
    }

}