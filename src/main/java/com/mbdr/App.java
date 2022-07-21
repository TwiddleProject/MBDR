package com.mbdr;

import java.io.*;
import java.util.*;

import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlSignature;

import com.mbdr.common.services.DefeasibleReasoner;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.formulabased.construction.BaseRankConstructor;
import com.mbdr.formulabased.reasoning.LexicographicBinaryReasoner;
import com.mbdr.formulabased.reasoning.LexicographicNaiveReasoner;
import com.mbdr.formulabased.reasoning.LexicographicPowersetReasoner;
import com.mbdr.formulabased.reasoning.LexicographicTernaryReasoner;
import com.mbdr.formulabased.reasoning.RationalBinaryReasoner;
import com.mbdr.formulabased.reasoning.RationalBinaryIndexingReasoner;
import com.mbdr.formulabased.reasoning.RationalDirectReasoner;
import com.mbdr.formulabased.reasoning.RationalIndexingReasoner;
import com.mbdr.formulabased.reasoning.RationalRegularReasoner;
import com.mbdr.modelbased.construction.LexicographicRefineConstructor;
import com.mbdr.modelbased.construction.RationalModelBaseRankConstructor;
import com.mbdr.modelbased.construction.RationalModelConstructor;
import com.mbdr.modelbased.reasoning.MinimalRankedEntailmentReasoner;
import com.mbdr.modelbased.structures.RankedInterpretation;
import com.mbdr.utils.parsing.KnowledgeBaseReader;
import com.mbdr.utils.parsing.Parsing;

public class App {

        public final static String KNOWLEDGE_BASE_DIR = "data/";
        public static void main(String[] args) {
                // TODO: Need to investigate normalising knowledge bases as twiddle statements

                String fileName = args.length >= 1 ? args[0] : "platypuses.txt";
                String rawQuery = args.length == 2 ? args[1] : "p|~w";

                try {
                        KnowledgeBaseReader reader = new KnowledgeBaseReader(KNOWLEDGE_BASE_DIR);
                        DefeasibleKnowledgeBase knowledgeBase = reader.parse(fileName);

                        System.out.println("----------------------------");
                        System.out.println(knowledgeBase);
                        System.out.println("----------------------------");

                        ArrayList<PlBeliefSet> ranked_KB = new BaseRankConstructor().construct(knowledgeBase);

                        System.out.println("BaseRank:");
                        System.out.println("----------------------------");
                        Object[] ranked_KB_Arr = ranked_KB.toArray();
                        System.out.println("∞" + " :\t" + ranked_KB_Arr[ranked_KB_Arr.length - 1]);
                        for (int rank_Index = ranked_KB_Arr.length - 2; rank_Index >= 0; rank_Index--) {
                                System.out.println(rank_Index + " :\t" + ranked_KB_Arr[rank_Index]);
                        }

                        System.out.println("----------------------------");
                        System.out.println("Testing query:\t" + rawQuery);

                        PlParser parser = new PlParser();
                        Implication query = (Implication) parser
                                        .parseFormula(Parsing.materialiseDefeasibleImplication(rawQuery));
                        
                        System.out.println("----------------------------");

                        RankedInterpretation rationalClosureModel = new RankedInterpretation(
                                new RationalModelConstructor()
                                .construct(knowledgeBase)
                        );
                        RankedInterpretation rationalClosureModelBaseRank = new RankedInterpretation(
                                new RationalModelBaseRankConstructor()
                                .construct(knowledgeBase)
                        );
                        RankedInterpretation lexicographicClosureModel = new LexicographicRefineConstructor(rationalClosureModel)
                                        .construct(knowledgeBase);

                        System.out.println("Rational Closure Ranked Model:\n" + rationalClosureModel);
                        System.out.println("----------------------------");
                        System.out.println("Rational Closure Ranked Model using BaseRank:\n" + rationalClosureModelBaseRank);
                        System.out.println("----------------------------");
                        System.out.println("Lexicographic Closure Ranked Model:\n" + lexicographicClosureModel);
                        System.out.println("----------------------------");

                        System.out.println("Query Results:");

                        DefeasibleReasoner[] checkers = {
                                new RationalDirectReasoner(ranked_KB, knowledgeBase),
                                new RationalRegularReasoner(ranked_KB),
                                new RationalIndexingReasoner(ranked_KB),
                                new RationalBinaryReasoner(ranked_KB),
                                new RationalBinaryIndexingReasoner(ranked_KB),
                                new MinimalRankedEntailmentReasoner(rationalClosureModel),
                                new LexicographicNaiveReasoner(ranked_KB),
                                new LexicographicPowersetReasoner(ranked_KB),
                                new LexicographicBinaryReasoner(ranked_KB),
                                new LexicographicTernaryReasoner(ranked_KB),
                                new MinimalRankedEntailmentReasoner(lexicographicClosureModel)
                        };

                        for(DefeasibleReasoner checker : checkers){
                                String template = "%-40s%s";
                                System.out.println(
                                        String.format(template, 
                                                checker.getClass().getSimpleName(), 
                                                checker.queryDefeasible(query))
                                );
                        }

                        System.out.println("----------------------------");

                } catch (FileNotFoundException e) {
                        System.out.println("Could not find knowledge base file!");
                        e.printStackTrace();
                } catch (ParserException e) {
                        System.out.println("Invalid formula in knowledge base!");
                        e.printStackTrace();
                } catch (IOException e) {
                        System.out.println("IO issue during formula parsing!");
                        e.printStackTrace();
                }
        }
}
