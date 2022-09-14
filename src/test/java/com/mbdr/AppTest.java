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
import com.mbdr.modelbased.construction.LexicographicCountModelRank;
import com.mbdr.modelbased.construction.LexicographicCountFormulaRank;
import com.mbdr.modelbased.construction.ModelRank;
import com.mbdr.modelbased.construction.CumulativeFormulaRank;
import com.mbdr.modelbased.construction.FormulaRank;
import com.mbdr.modelbased.construction.LexicographicCountCumulativeFormulaRank;
import com.mbdr.modelbased.reasoning.MinimalRankedEntailmentFormulaReasoner;
import com.mbdr.modelbased.reasoning.MinimalRankedEntailmentReasoner;
import com.mbdr.modelbased.structures.RankedFormulasInterpretation;
import com.mbdr.modelbased.structures.RankedInterpretation;
import com.mbdr.common.services.DefeasibleReasoner;
import com.mbdr.common.structures.DefeasibleKnowledgeBase;
import com.mbdr.formulabased.*;
import com.mbdr.formulabased.construction.BaseRank;
import com.mbdr.formulabased.reasoning.LexicographicBinaryReasoner;
import com.mbdr.formulabased.reasoning.LexicographicNaiveReasoner;
import com.mbdr.formulabased.reasoning.LexicographicPowersetReasoner;
import com.mbdr.formulabased.reasoning.LexicographicTernaryReasoner;
import com.mbdr.formulabased.reasoning.RationalBinaryReasoner;
import com.mbdr.formulabased.reasoning.RationalBinaryIndexingReasoner;
import com.mbdr.formulabased.reasoning.RationalDirectReasoner;
import com.mbdr.formulabased.reasoning.RationalIndexingReasoner;
import com.mbdr.formulabased.reasoning.RationalRegularReasoner;

public class AppTest 
{

    private static final String TEST_DIR_ROOT = "test_files/";
    private static final String TEST_KNOWLEDGE_BASES = TEST_DIR_ROOT + "knowledge_bases/";
    private static final String TEST_QUERIES = TEST_DIR_ROOT + "query_sets/";

    /**
     * Test entailment of all reasoners
     * 
     * @throws ParserException
     * @throws FileNotFoundException
     * @throws IOException
     */
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
            ArrayList<PlBeliefSet> baseRank = new BaseRank().construct(knowledgeBase);
            RankedInterpretation rationalClosureModel = new ModelRank().construct(knowledgeBase);
            RankedInterpretation lexicographicClosureModel = new LexicographicCountModelRank().construct(knowledgeBase);
            RankedFormulasInterpretation rationalClosureFormulaModel = new FormulaRank().construct(knowledgeBase);
            RankedFormulasInterpretation lexicographicClosureFormulaModel = new LexicographicCountFormulaRank().construct(knowledgeBase);
            RankedFormulasInterpretation rationalClosureCumulativeFormulaModel = new CumulativeFormulaRank().construct(knowledgeBase);
            RankedFormulasInterpretation lexicographicClosureCumulativeFormulaModel = new LexicographicCountCumulativeFormulaRank().construct(knowledgeBase);

            DefeasibleReasoner[] rationalClosureCheckers = {
                new RationalDirectReasoner(baseRank, knowledgeBase),
                new RationalRegularReasoner(baseRank),
                new RationalIndexingReasoner(baseRank),
                new RationalBinaryReasoner(baseRank),
                new RationalBinaryIndexingReasoner(baseRank),
                new MinimalRankedEntailmentReasoner(rationalClosureModel),
                new MinimalRankedEntailmentFormulaReasoner(rationalClosureFormulaModel),
                new MinimalRankedEntailmentFormulaReasoner(rationalClosureCumulativeFormulaModel)
            };

            DefeasibleReasoner[] lexicographicClosureCheckers = {
                new LexicographicNaiveReasoner(baseRank),
                new LexicographicPowersetReasoner(baseRank),
                new LexicographicBinaryReasoner(baseRank),
                new LexicographicTernaryReasoner(baseRank),
                new MinimalRankedEntailmentReasoner(lexicographicClosureModel),
                new MinimalRankedEntailmentFormulaReasoner(lexicographicClosureFormulaModel),
                new MinimalRankedEntailmentFormulaReasoner(lexicographicClosureCumulativeFormulaModel)
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
