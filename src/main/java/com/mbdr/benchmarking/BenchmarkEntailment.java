package com.mbdr.benchmarking;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.tweetyproject.logics.pl.syntax.Implication;
import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.syntax.PlSignature;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.mbdr.utils.parsing.*;
import com.mbdr.common.services.DefeasibleReasoner;
import com.mbdr.common.structures.*;
import com.mbdr.formulabased.construction.BaseRankConstructor;
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

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BenchmarkEntailment {

    public final static String knowledgeBaseDir = "data/benchmarking/knowledge_bases/";
    public final static String querySetDir = "data/benchmarking/query_sets/";

    @State(Scope.Benchmark) // all threads running the benchmark share the same state object.
    public static class BenchmarkState {

        @Param({ "knowledge_bases/" })
        String reasonerClassName;

        @Param({ "testing.txt" })
        String knowledgeBaseFileName;

        DefeasibleKnowledgeBase knowledgeBase;
        DefeasibleQuerySet querySet;
        DefeasibleReasoner reasoner;

        @Setup(Level.Trial) //The method is called once for each full run of the benchmark
        public void setup() {
            System.out.println("***** State initialization for benchmark trial *****");

            KnowledgeBaseReader knowledgeReader = new KnowledgeBaseReader(BenchmarkEntailment.knowledgeBaseDir);
            QueryReader queryReader = new QueryReader(BenchmarkEntailment.querySetDir);

            try {
                // Read in the knowledge base and query set and store in state object
                System.out.println("Parsing:\t" + knowledgeBaseFileName);
                this.knowledgeBase = knowledgeReader.parse(knowledgeBaseFileName);
                System.out.println("Parsing:\t" + queryReader.getQueryFileName(knowledgeBaseFileName));
                this.querySet = queryReader.parse(queryReader.getQueryFileName(knowledgeBaseFileName));
                // Using reflection, get class and create instance of reasoner
                Constructor<?> reasonerConstructor = Class.forName(this.reasonerClassName).getConstructor();
                this.reasoner = (DefeasibleReasoner) reasonerConstructor.newInstance();
                // Build the reasoner
                this.reasoner.build(this.knowledgeBase);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

        }

        @TearDown
        public void tearDown() {
            System.out.println("*****State cleanup happening here****");
        }
    }

    /*
     * JMH works as follows: users annotate the methods with @Benchmark, and
     * then JMH produces the generated code to run this particular benchmark as
     * reliably as possible. In general one might think about @Benchmark methods
     * as the benchmark "payload", the things we want to measure. The
     * surrounding infrastructure is provided by the harness itself.
     */

    
    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void entailment(BenchmarkState benchmarkState, Blackhole blackhole){

        for (PlFormula query : benchmarkState.querySet.getDefeasibleKnowledge()) {
            boolean queryAnswer = benchmarkState.reasoner.queryDefeasible((Implication) query);
            blackhole.consume(queryAnswer);
        }
        for (PlFormula query : benchmarkState.querySet.getPropositionalKnowledge()) {
            boolean queryAnswer = benchmarkState.reasoner.queryPropositional(query);
            blackhole.consume(queryAnswer);
        }

    }

    public static void main(String[] args) throws RunnerException {

        System.out.println("-----------------------------------------");
        System.out.println("Running benchmark harness...");
        System.out.println("-----------------------------------------");

        String[] reasonerClassNames={
            //Formula-based
            "com.mbdr.formulabased.reasoning.RationalRegularReasoner",
            "com.mbdr.formulabased.reasoning.RationalDirectReasoner",
            "com.mbdr.formulabased.reasoning.RationalIndexingReasoner",
            "com.mbdr.formulabased.reasoning.RationalBinaryReasoner",
            "com.mbdr.formulabased.reasoning.RationalBinaryIndexingReasoner",
            "com.mbdr.formulabased.reasoning.LexicographicNaiveReasoner",
            "com.mbdr.formulabased.reasoning.LexicographicPowersetReasoner",
            "com.mbdr.formulabased.reasoning.LexicographicBinaryReasoner",
            "com.mbdr.formulabased.reasoning.LexicographicTernaryReasoner",
            // Model-based
            "com.mbdr.modelbased.reasoning.RationalModelReasoner",
            "com.mbdr.modelbased.reasoning.LexicographicModelReasoner"
        };

        // TODO Add default constructors (engines) for each reasoner

        String[] knowledgeBaseFileNames = new String[0];
        knowledgeBaseFileNames = new FileReader(knowledgeBaseDir)
                                .getFileNames()
                                .toArray(knowledgeBaseFileNames);

        Options benchmarkOptions = new OptionsBuilder()
                .include("^com.mbdr.benchmarking.BenchmarkEntailment.*")
                .param("reasonerClassName", reasonerClassNames)
                .param("knowledgeBaseFileName", knowledgeBaseFileNames)
                .resultFormat(ResultFormatType.CSV)
                .result("results/entailment.csv")
                .build();

        Collection<RunResult> results = new Runner(benchmarkOptions).run();
        for (RunResult result : results) {
            Result<?> r = result.getPrimaryResult();
            System.out.println(r);
        }

    }

}