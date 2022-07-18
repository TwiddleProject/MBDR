package com.mbdr;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;

import com.mbdr.services.DefeasibleQueryChecker;
import com.mbdr.structures.DefeasibleKnowledgeBase;
import com.mbdr.utils.parsing.*;
import com.mbdr.modelbased.*;
import com.mbdr.formulabased.*;

public class AppTest 
{
    // TODO Make checkers global (so only need to add new methods in one place e.g. App)

    private static final String TEST_DIR_ROOT = "test_files/";
    private static final String TEST_KNOWLEDGE_BASES = TEST_DIR_ROOT + "knowledge_bases/";
    private static final String TEST_QUERIES = TEST_DIR_ROOT + "queries/";
    private static final String TEST_MODELS = TEST_DIR_ROOT + "models/";
    private static final String TEST_BASE_RANKS = TEST_DIR_ROOT + "base_ranks/";

    @Test
    public void testEntailment() throws ParserException, FileNotFoundException, IOException
    {
        KnowledgeBaseReader reader = new KnowledgeBaseReader(TEST_KNOWLEDGE_BASES);

        // For each knowledge base file, read in knowledge and corresponding query file
        for(String knowledgeBaseFileName : reader.getFileNames()){

            System.out.println("Query File: " + knowledgeBaseFileName);

            DefeasibleKnowledgeBase knowledgeBase = Parser.parseFormulas(
                reader.readFileLines(knowledgeBaseFileName)
            );

            QueryReader queryReader = new QueryReader(TEST_QUERIES);
            ArrayList<String> queries = queryReader.readFileLines(queryReader.getQueryFileName(knowledgeBaseFileName));
            
            // Rank, model and checker construction
            ArrayList<PlBeliefSet> baseRank = new BaseRankConstructor().construct(knowledgeBase);
            RankedInterpretation rationalClosureModel = new RankedInterpretation(
                new RationalModelConstructor().construct(knowledgeBase)
            );
            RankedInterpretation lexicographicClosureModel = new LexicographicModelConstructor().construct(knowledgeBase);

            DefeasibleQueryChecker[] rationalClosureCheckers = {
                new RationalDirectChecker(baseRank, knowledgeBase),
                new RationalRegularChecker(baseRank),
                new RationalIndexingChecker(baseRank),
                new RationalBinaryChecker(baseRank),
                new RationalBinaryIndexingChecker(baseRank),
                new MinimalRankedEntailmentChecker(rationalClosureModel)
            };

            DefeasibleQueryChecker[] lexicographicClosureCheckers = {
                new LexicographicNaiveChecker(baseRank),
                new LexicographicPowersetChecker(baseRank),
                new LexicographicBinaryChecker(baseRank),
                new LexicographicTernaryChecker(baseRank),
                new MinimalRankedEntailmentChecker(lexicographicClosureModel)
            };

            // For each type of entailment checker
            for(DefeasibleQueryChecker[] checkers : 
            new DefeasibleQueryChecker[][]{
                rationalClosureCheckers, 
                lexicographicClosureCheckers
            }){
                // For each query, determine entailment using the checkers
                for(String query: queries){
                    ArrayList<Boolean> results = new ArrayList<>(checkers.length);
                    System.out.println("Query: " + query);
                    for(DefeasibleQueryChecker checker : checkers){
                        results.add(
                            checker.query(query)
                        );
                        System.out.println(checker.getClass().getSimpleName() + ": " + results.get(results.size()-1));
                    }
                    // assertTrue(allEntriesEqual(results));
                }
            }

        }
    }

    private static <T> boolean allEntriesEqual(ArrayList<T> list){
        T first = list.get(0);
        for(T item : list){
            if(!item.equals(first)) return false;
        }
        return true;
    } 
}
