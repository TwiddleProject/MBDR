package com.mbdr.formulabased;

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

import com.mbdr.services.DefeasibleQueryChecker;
import com.mbdr.services.RankConstructor;
import com.mbdr.structures.DefeasibleKnowledgeBase;
import com.mbdr.utils.exceptions.MissingRankConstructor;
import com.mbdr.utils.exceptions.MissingRanking;
import com.mbdr.utils.parsing.Parser;

public class LexicographicPowersetChecker implements DefeasibleQueryChecker{

    private ArrayList<PlBeliefSet> baseRank;
    private RankConstructor<ArrayList<PlBeliefSet>> constructor;

    public LexicographicPowersetChecker(ArrayList<PlBeliefSet> baseRank){
        this.baseRank = new ArrayList<>(baseRank);
    }

    public LexicographicPowersetChecker(RankConstructor<ArrayList<PlBeliefSet>> constructor){
        this.constructor = constructor;
    }

    @Override
    public void build(DefeasibleKnowledgeBase knowledge){
        if(this.constructor == null) throw new MissingRankConstructor("Cannot build base rank without RankConstructor.");
        this.baseRank = constructor.construct(knowledge);
    }

    @Override
    public boolean queryPropositional(PlFormula formula){
        if(this.baseRank == null) throw new MissingRanking("Base rank has not been constructed.");
        return queryDefeasible(Parser.normalizePropositionalFormula(formula));
    }

    @Override
    public boolean queryDefeasible(Implication formula){
        if(this.baseRank == null) throw new MissingRanking("Base rank has not been constructed.");
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
                    // System.out.println("Each possible refinement's first formula is subject to
                    // removal, and at each check each subsequent formula is removed until no more
                    // removals can be made. The refinements contain:" +
                    // this.baseRank.get(0).toString());
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
                        // System.out.println("ranked kb" + this.baseRank.toString());
                        if (!classicalReasoner.query(Utils.combine(this.baseRank),
                                new Negation(((Implication) formula).getFormulas().getFirst()))) {
                            // System.out.println((new Negation(((Implication)
                            // formula).getFormulas().getFirst())).toString() + " is not entailed by this
                            // refinement.");
                            // System.out.println("We now check whether or not the formula" +
                            // formula.toString() + " is entailed by " + combine(this.baseRank).toString());
                            if (classicalReasoner.query(Utils.combine(this.baseRank), formula)) {
                                return true;
                            } else {
                                return false;
                            }

                        } else {
                            // System.out.println("No it does not! We carry on checking:");
                        }
                    }
                    // System.out.println("Since our refinements are empty, we remove the top
                    // rank.");
                    this.baseRank.remove(0);
                } else {
                    // System.out.println("It does! So we remove the top rank.");
                    this.baseRank.remove(0);
                }
            } else {
                // System.out.println("It does not!");
                break;
            }

        }

        if (Utils.combine(this.baseRank).size() == 1) {
            // System.out.println("We now check whether or not the
            // formula"+formula.toString()+" is entailed by "+combine(this.baseRank).toString());
            // Since we do not check the refinements of the infinite rank
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