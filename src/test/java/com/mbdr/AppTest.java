package com.mbdr;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;

import com.mbdr.utils.parsing.*;
import com.mbdr.modelbased.*;
import com.mbdr.modelbased.construction.LexicographicRefineConstructor;
import com.mbdr.modelbased.construction.RationalModelConstructor;
import com.mbdr.modelbased.reasoning.MinimalRankedEntailmentReasoner;
import com.mbdr.modelbased.structures.RankedInterpretation;
import com.mbdr.common.services.DefeasibleReasoner;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.formulabased.*;
import com.mbdr.formulabased.construction.BaseRankConstructor;
import com.mbdr.formulabased.reasoning.LexicographicBinaryReasoner;
import com.mbdr.formulabased.reasoning.LexicographicNaiveReasoner;
import com.mbdr.formulabased.reasoning.LexicographicPowersetReasoner;
import com.mbdr.formulabased.reasoning.LexicographicTernaryReasoner;
import com.mbdr.formulabased.reasoning.RationalBinaryReasoner;
import com.mbdr.formulabased.reasoning.RationalBinaryIndexingChecker;
import com.mbdr.formulabased.reasoning.RationalDirectReasoner;
import com.mbdr.formulabased.reasoning.RationalIndexingReasoner;
import com.mbdr.formulabased.reasoning.RationalRegularReasoner;

public class AppTest 
{
    // TODO Make checkers global (so only need to add new methods in one place e.g. App)

    private static final String TEST_DIR_ROOT = "test_files/";
    private static final String TEST_KNOWLEDGE_BASES = TEST_DIR_ROOT + "knowledge_bases/";
    private static final String TEST_QUERIES = TEST_DIR_ROOT + "query_sets/";
    private static final String TEST_MODELS = TEST_DIR_ROOT + "models/";
    private static final String TEST_BASE_RANKS = TEST_DIR_ROOT + "base_ranks/";

    @Test
    public void testEntailment() throws ParserException, FileNotFoundException, IOException
    {
        KnowledgeBaseReader reader = new KnowledgeBaseReader(TEST_KNOWLEDGE_BASES);

        // For each knowledge base file, read in knowledge and corresponding query file
        for(String knowledgeBaseFileName : reader.getFileNames()){

            System.out.println("Query File: " + knowledgeBaseFileName);

            DefeasibleKnowledgeBase knowledgeBase = reader.parse(knowledgeBaseFileName);

            QueryReader queryReader = new QueryReader(TEST_QUERIES);
            ArrayList<String> queries = queryReader.readFileLines(queryReader.getQueryFileName(knowledgeBaseFileName));
            
            // Rank, model and checker construction
            ArrayList<PlBeliefSet> baseRank = new BaseRankConstructor().construct(knowledgeBase);
            RankedInterpretation rationalClosureModel = new RankedInterpretation(
                new RationalModelConstructor().construct(knowledgeBase)
            );
            RankedInterpretation lexicographicClosureModel = new LexicographicRefineConstructor().construct(knowledgeBase);

            DefeasibleReasoner[] rationalClosureCheckers = {
                new RationalDirectReasoner(baseRank, knowledgeBase),
                new RationalRegularReasoner(baseRank),
                new RationalIndexingReasoner(baseRank),
                new RationalBinaryReasoner(baseRank),
                new RationalBinaryIndexingChecker(baseRank),
                new MinimalRankedEntailmentReasoner(rationalClosureModel)
            };

            DefeasibleReasoner[] lexicographicClosureCheckers = {
                new LexicographicNaiveReasoner(baseRank),
                new LexicographicPowersetReasoner(baseRank),
                new LexicographicBinaryReasoner(baseRank),
                new LexicographicTernaryReasoner(baseRank),
                new MinimalRankedEntailmentReasoner(lexicographicClosureModel)
            };

            // For each type of entailment checker
            for(DefeasibleReasoner[] checkers : 
            new DefeasibleReasoner[][]{
                rationalClosureCheckers, 
                lexicographicClosureCheckers
            }){
                // For each query, determine entailment using the checkers
                for(String query: queries){
                    ArrayList<Boolean> results = new ArrayList<>(checkers.length);
                    System.out.println("Query: " + query);
                    for(DefeasibleReasoner checker : checkers){
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
