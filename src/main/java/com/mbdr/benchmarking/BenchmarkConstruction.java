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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import com.mbdr.utils.parsing.*;
import com.mbdr.common.services.RankConstructor;
import com.mbdr.common.structures.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BenchmarkConstruction {
    
    public final static String knowledgeBaseDir = "data/benchmarking/";

    @State(Scope.Benchmark) // all threads running the benchmark share the same state object.
    public static class BenchmarkState {

        @Param({ })
        String constructorClassName;

        @Param({ })
        String knowledgeBaseFileName;

        DefeasibleKnowledgeBase knowledgeBase;
        Constructor<?> constructor;

        @Setup(Level.Trial) //The method is called once for each full run of the benchmark
        public void setup() {
            System.out.println("***** State initialization for benchmark trial *****");

            KnowledgeBaseReader knowledgeReader = new KnowledgeBaseReader(BenchmarkConstruction.knowledgeBaseDir);

            try {
                // Read in the knowledge base and store in state object
                System.out.println("Parsing:\t" + knowledgeBaseFileName);
                this.knowledgeBase = knowledgeReader.parse(knowledgeBaseFileName);
                // Using reflection, get constructor for creating instances of RankConstructor
                Class<?> rankConstructorClass = Class.forName(this.constructorClassName);
                this.constructor = rankConstructorClass.getConstructor();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

        }

        @TearDown
        public void tearDown() {
            System.out.println("***** State cleanup for benchmark trial *****");
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
    public void construction(BenchmarkState benchmarkState, Blackhole blackhole)
            throws InterruptedException, ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, 
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // Create new instance of RankConstructor
        RankConstructor<?> rankConstructor = (RankConstructor<?>) benchmarkState.constructor.newInstance();
        // Construct ranking
        Object ranking = rankConstructor.construct(benchmarkState.knowledgeBase);
        blackhole.consume(ranking); // consume to avoid dead code elimination just in case?
    }

    public static void main(String[] args) throws RunnerException {

        System.out.println("-----------------------------------------");
        System.out.println("Running benchmark harness...");
        System.out.println("-----------------------------------------");

        String[] constructorClassNames={
            "com.mbdr.formulabased.construction.BaseRankConstructor",
            "com.mbdr.formulabased.construction.BaseRankConstructorJoel",
            "com.mbdr.modelbased.construction.LexicographicBasicConstructor",
            "com.mbdr.modelbased.construction.RationalModelConstructor",
            "com.mbdr.modelbased.construction.RationalModelBaseRankConstructor"
        };

        String[] knowledgeBaseFileNames = new String[0];
        knowledgeBaseFileNames = new FileReader(knowledgeBaseDir)
                                .getFileNames()
                                .toArray(knowledgeBaseFileNames);

        Options benchmarkOptions = new OptionsBuilder()
                .include("^com.mbdr.benchmarking.BenchmarkConstruction.*")
                .param("constructorClassName", constructorClassNames)
                .param("knowledgeBaseFileName", knowledgeBaseFileNames)
                .resultFormat(ResultFormatType.CSV)
                .result("results/construction.csv")
                .build();

        Collection<RunResult> results = new Runner(benchmarkOptions).run();
        for (RunResult result : results) {
            Result<?> r = result.getPrimaryResult();
            System.out.println(r);
        }

     }

}