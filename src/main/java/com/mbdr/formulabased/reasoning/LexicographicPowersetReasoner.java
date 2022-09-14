package com.mbdr.formulabased.reasoning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Weakening lexicographic algorithm.
 * Implementation from SCADR project (2021).
 */
public class LexicographicPowersetReasoner implements DefeasibleReasoner{

    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;

    /**
     * Default constructor
     */
    public LexicographicPowersetReasoner(){
        this(new BaseRank());
    }

    /**
     * Parameterized constructor
     * 
     * @param baseRank The base rank of the knowledge
     */
    public LexicographicPowersetReasoner(ArrayList<PlBeliefSet> baseRank){
        this.baseRank = new ArrayList<>(baseRank);
    }

    /**
     * Parameterized constructor
     * 
     * @param constructor The base rank constructor
     */
    public LexicographicPowersetReasoner(RankConstructor<ArrayList<PlBeliefSet>> constructor){
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
        PlParser parser = new PlParser();
        SatSolver.setDefaultSolver(new Sat4jSolver());

        while (this.baseRank.size() > 1) {

            if (classicalReasoner.query(Utils.combine(this.baseRank),
                    new Negation(formula.getFormulas().getFirst()))) {
                if (this.baseRank.get(0).size() > 1) {
                    Object[] rank = this.baseRank.get(0).toArray();
                    List<Set<Object>> sortedRank = sortList(rank);

                    ArrayList<String> refinements = new ArrayList<>(combineRefine(sortedRank));
                    for (String f : refinements) {

                        PlBeliefSet combSet = new PlBeliefSet();
                        try {
                            combSet.add((PlFormula) parser.parseFormula(f));
                        } catch (IOException e) {
                            throw new InvalidFormula("Invalid formula encountered during entailment check.");
                        }
                        this.baseRank.set(0, combSet);
                        if (!classicalReasoner.query(Utils.combine(this.baseRank),
                                new Negation(((Implication) formula).getFormulas().getFirst()))) {
                            if (classicalReasoner.query(Utils.combine(this.baseRank), formula)) {
                                return true;
                            } else {
                                return false;
                            }

                        }
                    }
                    this.baseRank.remove(0);
                } else {
                    this.baseRank.remove(0);
                }
            } else {
                break;
            }

        }

        if (Utils.combine(this.baseRank).size() == 1) {
            if (classicalReasoner.query(Utils.combine(this.baseRank), formula)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Daniel's PowerSet function that returns the subsets of the rank
     * 
     * @param <T>
     * @param originalSet
     * @return
     */
    private static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
        Set<Set<T>> sets = new HashSet<Set<T>>();
        if (originalSet.size() == 0) {
            sets.add(new HashSet<T>());
            return sets;
        }
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<T>(list.subList(1, list.size()));
        for (Set<T> set : powerSet(rest)) {

            Set<T> newSet = new HashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);

        }
        return sets;
    }

    /**
     * Daniel's sortList function - sorts list of sets based on their size
     * 
     * @param rank
     * @return
     */
    public static List<Set<Object>> sortList(Object[] rank) {
        class SizeComparator implements Comparator<Set<?>> {

            @Override
            public int compare(Set<?> o1, Set<?> o2) {
                return Integer.valueOf(o1.size()).compareTo(o2.size());
            }
        }
        Set<Object> mySet = new HashSet<Object>();

        for (Object a : rank) {
            mySet.add(a.toString());
        }
        List<Set<Object>> list = new ArrayList<Set<Object>>(powerSet(mySet));
        Collections.sort(list, new SizeComparator());
        list.remove(0);
        list.remove(list.size() - 1);
        return list;
    }

    /**
     * Daniel's combineRefine function that combines the list of subsets using
     * conjunction and disjunction.
     * 
     * @param set
     * @return
     */
    public static ArrayList<String> combineRefine(List<Set<Object>> set) {
        Map<Integer, List<Set<Object>>> subSet = new HashMap<>();
        for (Set<Object> s : set) {
            if (!subSet.containsKey(s.size())) {
                List<Set<Object>> temp = new ArrayList<>();
                temp.add(s);
                subSet.put(s.size(), temp);
            } else {
                List<Set<Object>> temp = new ArrayList<>();
                temp = subSet.get(s.size());
                temp.add(s);
                subSet.put(s.size(), temp);
            }
        }
        ArrayList<String> refinements = new ArrayList<>();

        for (int i = subSet.size(); i > 0; i--) {
            String combined = new String();
            for (int j = 0; j < subSet.get(i).size(); j++) {
                Object[] ref = subSet.get(i).get(j).toArray();
                combined += "(";
                if (ref.length > 1) {
                    for (Object o : ref) {
                        combined += o.toString() + " && ";
                    }
                    combined = combined.substring(0, combined.length() - 4) + ") || ";
                } else {
                    combined += ref[0].toString() + ") || ";
                }

            }
            combined = combined.substring(0, combined.length() - 4);
            refinements.add(combined);
        }
        return refinements;
    }
}
