package com.mbdr;

import java.io.*;
import java.util.*;

import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlSignature;

import com.mbdr.formulabased.BaseRankConstructor;
import com.mbdr.formulabased.LexicographicBinaryChecker;
import com.mbdr.formulabased.LexicographicNaiveChecker;
import com.mbdr.formulabased.LexicographicPowersetChecker;
import com.mbdr.formulabased.LexicographicTernaryChecker;
import com.mbdr.formulabased.RationalBinaryChecker;
import com.mbdr.formulabased.RationalBinaryIndexingChecker;
import com.mbdr.formulabased.RationalDirectChecker;
import com.mbdr.formulabased.RationalIndexingChecker;
import com.mbdr.formulabased.RationalRegularChecker;
import com.mbdr.modelbased.LexicographicModelConstructor;
import com.mbdr.modelbased.MinimalRankedEntailmentChecker;
import com.mbdr.modelbased.RankedInterpretation;
import com.mbdr.modelbased.RationalModelBaseRankConstructor;
import com.mbdr.modelbased.RationalModelConstructor;
import com.mbdr.services.DefeasibleQueryChecker;
import com.mbdr.structures.DefeasibleKnowledgeBase;
import com.mbdr.utils.parsing.KnowledgeBaseReader;
import com.mbdr.utils.parsing.Parser;

public class App {

        public final static String KNOWLEDGE_BASE_DIR = "knowledge_bases/";
        public static void main(String[] args) {
                // TODO: Need to investigate normalising knowledge bases as twiddle statements

                String fileName = args.length >= 1 ? args[0] : "platypuses.txt";
                String rawQuery = args.length == 2 ? args[1] : "p|~w";

                try {
                        KnowledgeBaseReader reader = new KnowledgeBaseReader(KNOWLEDGE_BASE_DIR);
                        ArrayList<String> rawFormulas = reader.readFileLines(fileName);
                        DefeasibleKnowledgeBase knowledgeBase = Parser.parseFormulas(rawFormulas);

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
                                        .parseFormula(Parser.materialiseDefeasibleImplication(rawQuery));
                        
                        System.out.println("----------------------------");

                        RankedInterpretation rationalClosureModel = new RankedInterpretation(
                                new RationalModelConstructor()
                                .construct(knowledgeBase)
                        );
                        RankedInterpretation rationalClosureModelBaseRank = new RankedInterpretation(
                                new RationalModelBaseRankConstructor()
                                .construct(knowledgeBase)
                        );
                        RankedInterpretation lexicographicClosureModel = new LexicographicModelConstructor(rationalClosureModel)
                                        .construct(knowledgeBase);

                        System.out.println("Rational Closure Ranked Model:\n" + rationalClosureModel);
                        System.out.println("----------------------------");
                        System.out.println("Rational Closure Ranked Model using BaseRank:\n" + rationalClosureModelBaseRank);
                        System.out.println("----------------------------");
                        System.out.println("Lexicographic Closure Ranked Model:\n" + lexicographicClosureModel);
                        System.out.println("----------------------------");

                        System.out.println("Query Results:");

                        DefeasibleQueryChecker[] checkers = {
                                new RationalDirectChecker(ranked_KB, knowledgeBase),
                                new RationalRegularChecker(ranked_KB),
                                new RationalIndexingChecker(ranked_KB),
                                new RationalBinaryChecker(ranked_KB),
                                new RationalBinaryIndexingChecker(ranked_KB),
                                new MinimalRankedEntailmentChecker(rationalClosureModel),
                                new LexicographicNaiveChecker(ranked_KB),
                                new LexicographicPowersetChecker(ranked_KB),
                                new LexicographicBinaryChecker(ranked_KB),
                                new LexicographicTernaryChecker(ranked_KB),
                                new MinimalRankedEntailmentChecker(lexicographicClosureModel)
                        };

                        for(DefeasibleQueryChecker checker : checkers){
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
