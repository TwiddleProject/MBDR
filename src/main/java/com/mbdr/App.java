package com.mbdr;

import java.io.*;
import java.util.*;

import org.tweetyproject.logics.pl.parser.PlParser;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.syntax.PlSignature;

import com.mbdr.formulabased.BaseRank;
import com.mbdr.modelbased.EntailmentChecker;
import com.mbdr.structures.DefeasibleKnowledgeBase;
import com.mbdr.structures.RankedInterpretation;
import com.mbdr.utils.parsing.KnowledgeBaseReader;
import com.mbdr.utils.parsing.Parser;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;

public class App {

        public final static String KNOWLEDGE_BASE_DIR = "knowledge_bases/";

        public static void main(String[] args) {
                // TODO: Need to investigate normalising knowledge bases as twiddle statements

                String fileName = "platypuses.txt";

                try {
                        KnowledgeBaseReader reader = new KnowledgeBaseReader(KNOWLEDGE_BASE_DIR);
                        ArrayList<String> rawFormulas = reader.readFormulasFromFile(fileName);
                        DefeasibleKnowledgeBase knowledgeBase = Parser.parseFormulas(rawFormulas);

                        System.out.println("----------------------------");
                        System.out.println(knowledgeBase);
                        System.out.println("----------------------------");

                        ArrayList<PlBeliefSet> ranked_KB = BaseRank.BaseRankDirectImplementation(knowledgeBase);

                        System.out.println("BaseRank:");
                        System.out.println("----------------------------");
                        Object[] ranked_KB_Arr = ranked_KB.toArray();
                        System.out.println("∞" + " :\t" + ranked_KB_Arr[ranked_KB_Arr.length - 1]);
                        for (int rank_Index = ranked_KB_Arr.length - 2; rank_Index >= 0; rank_Index--) {
                                System.out.println(rank_Index + " :\t" + ranked_KB_Arr[rank_Index]);
                        }

                        String rawQuery = "p |~ w";

                        System.out.println("----------------------------");
                        System.out.println("Testing query:\t" + rawQuery);

                        PlParser parser = new PlParser();
                        Implication query = (Implication) parser
                                        .parseFormula(Parser.materialiseDefeasibleImplication(rawQuery));

                        System.out.println("Materialised query:\t" + query.toString());
                        System.out.println(
                                        "Answer to query (RC direct implementation):\t\t"
                                                        + com.mbdr.formulabased.RationalClosure
                                                                        .RationalClosureDirectImplementation(
                                                                                        knowledgeBase, query));

                        System.out.println(
                                        "Answer to query (RC Joel's Regular):\t\t\t"
                                                        + com.mbdr.formulabased.RationalClosure
                                                                        .RationalClosureJoelRegular(ranked_KB,
                                                                                        rawQuery));

                        com.mbdr.formulabased.RationalClosure RC_Indexing = new com.mbdr.formulabased.RationalClosure();

                        System.out.println(
                                        "Answer to query (RC Joel's Regular Indexing):\t\t"
                                                        + RC_Indexing.RationalClosureJoelRegularIndexing(ranked_KB,
                                                                        rawQuery));

                        System.out.println(
                                        "Answer to query (RC Joel's Binary Search):\t\t"
                                                        + com.mbdr.formulabased.RationalClosure
                                                                        .RationalClosureJoelBinarySearch(ranked_KB,
                                                                                        rawQuery));

                        com.mbdr.formulabased.RationalClosure RC_Binary_Indexing = new com.mbdr.formulabased.RationalClosure();

                        System.out.println(
                                        "Answer to query (RC Joel's Binary Indexing Search):\t"
                                                        + RC_Binary_Indexing.RationalClosureJoelBinarySearchIndexing(
                                                                        ranked_KB, rawQuery));

                        System.out.println(
                                        "Answer to query (LC Daniels's Naive):\t\t\t"
                                                        + com.mbdr.formulabased.LexicographicClosure
                                                                        .LexicographicClosureDanielNaive(ranked_KB,
                                                                                        query));
                        System.out.println(
                                        "Answer to query (LC Daniels's Powerset):\t\t"
                                                        + com.mbdr.formulabased.LexicographicClosure
                                                                        .LexicographicClosureDanielPowerset(ranked_KB,
                                                                                        query));

                        RankedInterpretation rationalClosureModel = new RankedInterpretation(com.mbdr.modelbased.RationalClosure
                                        .ConstructRankedModel(knowledgeBase, null));
                        RankedInterpretation lexicographicClosureModel = com.mbdr.modelbased.LexicographicClosure
                                        .refine(knowledgeBase, rationalClosureModel);
                        EntailmentChecker rcChecker = new EntailmentChecker(rationalClosureModel);
                        EntailmentChecker lcChecker = new EntailmentChecker(lexicographicClosureModel);

                        System.out.println("----------------------------");
                        System.out.println("Rational Closure Ranked Model:");

                        System.out.println(rationalClosureModel);

                        System.out.println(
                                        "Answer to query:\t" + rcChecker.query(rawQuery));
                        System.out.println("----------------------------");
                        System.out.println("Lexicographic Closure Ranked Model:");

                        System.out.println(lexicographicClosureModel);

                        System.out.println(
                                        "Answer to query:\t" + lcChecker.query(rawQuery));
                        System.out.println("----------------------------");
                        System.out.println("ConstructRankedModelBaseRank:");
                        System.out.println("----------------------------");

                        PlBeliefSet kBunion = knowledgeBase.union();
                        System.out.println("knowledgeBase union:\t" + kBunion);
                        PlSignature kBsignature = kBunion.getMinimalSignature();
                        System.out.println("knowledgeBase signature:\t" + kBsignature);

                        Set<NicePossibleWorld> kB_PossibleWorlds = NicePossibleWorld
                                        .getAllPossibleWorlds(kBsignature.toCollection());
                        System.out.println("kB_PossibleWorlds:\t" + kB_PossibleWorlds);

                        RankedInterpretation rationalClosureModelBR = new RankedInterpretation(com.mbdr.modelbased.RationalClosure
                                        .ConstructRankedModelBaseRank(knowledgeBase, kB_PossibleWorlds));

                        System.out.println(rationalClosureModelBR);

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
