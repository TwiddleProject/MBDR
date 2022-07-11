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
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.syntax.PlSignature;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.mbdr.utils.parsing.*;
import com.mbdr.structures.*;

import com.mbdr.formulabased.BaseRank;
import com.mbdr.modelbased.MinimalRankedEntailmentChecker;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BenchMark {
    @State(Scope.Benchmark) // all threads running the benchmark share the same state object.
    public static class StateObj {

        @Param({ "knowledge_bases/" })
        String kBDirectory;

        @Param({ "testing.txt" })
        String kBFileName;

        @Param({ "testingQueries.txt" })
        String queriesFileName;

        @Param({ "0" })
        int benchmarkGroup;

        DefeasibleKnowledgeBase knowledgeBase;
        ArrayList<PlBeliefSet> ranked_KB;
        Set<NicePossibleWorld> KB_U;
        RankedInterpretation rationalModel;
        RankedInterpretation lexicographicModel;
        ArrayList<String> rawQueries;

        @Setup(Level.Trial) // Level.Trial is the default level. The method is called once for each full run
        // of the benchmark
        public void setup() {
            System.out.println("*****State initialization happening here****");

            // TODO: some refactoring is necessary but this is fine for first results

            System.out.println("reading in:\t" + kBFileName);
            KnowledgeBaseReader reader = new KnowledgeBaseReader(kBDirectory);

            try {
                // Read in the knowledge base and store in state object
                ArrayList<String> rawFormulas = reader.readFormulasFromFile(kBFileName);
                this.knowledgeBase = Parser.parseFormulas(rawFormulas);

            } catch (Exception e) {
                // TODO: handle exception
            }

            PlSignature KB_atoms = this.knowledgeBase.union().getMinimalSignature();

            // BaseRank Ranked Knowledge Base Benchmarks - 0
            // Formula-based Rational Closure Entailment Benchmarks - 1
            // Model-based Ranked Model Construction Benchmarks - 2
            // Model-based Rational Closure Entailment Benchmarks - 3

            switch (benchmarkGroup) {
                case 0:
                    break;
                case 1:
                    // Rank the knowledge base using baserank - for benchmarking formulabased
                    // entailment checking
                    this.ranked_KB = BaseRank.BaseRankDirectImplementation(this.knowledgeBase);

                    System.out.println("reading in:\t" + queriesFileName);
                    // Read in all the queries from the query file

                    KnowledgeBaseReader readerTemp = new KnowledgeBaseReader("knowledge_bases/");

                    try {
                        this.rawQueries = readerTemp.readFormulasFromFile(queriesFileName);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    break;
                case 2:
                    // Enumerate all possible worlds w.r.t. atoms of KB to reduce benchmarking
                    // runtime across repeated runs
                    System.out.print("Getting all possible worlds w.r.t. atoms:\t" + KB_atoms);
                    this.KB_U = NicePossibleWorld.getAllPossibleWorlds(KB_atoms.toCollection());
                    break;
                case 3:
                    // Enumerate all possible worlds w.r.t. atoms of KB to reduce benchmarking
                    // runtime across repeated runs
                    System.out.print("Getting all possible worlds w.r.t. atoms:\t" + KB_atoms);
                    this.KB_U = NicePossibleWorld.getAllPossibleWorlds(KB_atoms.toCollection());

                    // Construct the ranked models for RC and LC for query benchmarking
                    this.rationalModel = new RankedInterpretation(com.mbdr.modelbased.RationalClosure
                            .ConstructRankedModel(knowledgeBase, this.KB_U));

                    this.lexicographicModel = com.mbdr.modelbased.LexicographicClosure
                            .refine(knowledgeBase, rationalModel);

                    System.out.println("reading in:\t" + queriesFileName);
                    // Read in all the queries from the query file

                    KnowledgeBaseReader readerTemp2 = new KnowledgeBaseReader("knowledge_bases/");

                    try {
                        this.rawQueries = readerTemp2.readFormulasFromFile(queriesFileName);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }

                    break;
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

    // -----------------------------------------------------------------------------------------------------------
    // BaseRank Ranked Knowledge Base Benchmarks:
    // -----------------------------------------------------------------------------------------------------------

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void formulabased_baserank_direct_implementation(StateObj stateObj, Blackhole blackhole)
            throws InterruptedException {
        ArrayList<PlBeliefSet> ranked_KB = BaseRank.BaseRankDirectImplementation(stateObj.knowledgeBase);
        blackhole.consume(ranked_KB);
    }

    // -----------------------------------------------------------------------------------------------------------
    // Formula-based Rational Closure Entailment Benchmarks:
    // -----------------------------------------------------------------------------------------------------------

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void formulabased_RC_direct_implementation(StateObj stateObj, Blackhole blackhole) {

        for (String rawQuery : stateObj.rawQueries) {
            try {
                boolean queryAnswer = com.mbdr.formulabased.RationalClosure
                        .RationalClosureDirectImplementation_Benchmarking(
                                stateObj.ranked_KB,
                                stateObj.knowledgeBase,
                                rawQuery);
                blackhole.consume(queryAnswer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void formulabased_RC_Joel_regular(StateObj stateObj, Blackhole blackhole) {

        for (String rawQuery : stateObj.rawQueries) {
            try {
                boolean queryAnswer = com.mbdr.formulabased.RationalClosure.RationalClosureJoelRegular(
                        stateObj.ranked_KB,
                        rawQuery);
                blackhole.consume(queryAnswer);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void formulabased_RC_Joel_binary_search(StateObj stateObj, Blackhole blackhole) {

        for (String rawQuery : stateObj.rawQueries) {
            try {
                boolean queryAnswer = com.mbdr.formulabased.RationalClosure.RationalClosureJoelBinarySearch(
                        stateObj.ranked_KB,
                        rawQuery);
                blackhole.consume(queryAnswer);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void formulabased_RC_Joel_regular_indexing(StateObj stateObj, Blackhole blackhole) {

        com.mbdr.formulabased.RationalClosure RC_Indexing = new com.mbdr.formulabased.RationalClosure();

        for (String rawQuery : stateObj.rawQueries) {
            try {
                boolean queryAnswer = RC_Indexing.RationalClosureJoelRegularIndexing(
                        stateObj.ranked_KB,
                        rawQuery);
                blackhole.consume(queryAnswer);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void formulabased_RC_Joel_binary_search_indexing(StateObj stateObj, Blackhole blackhole) {

        com.mbdr.formulabased.RationalClosure RC_Binary_Indexing = new com.mbdr.formulabased.RationalClosure();

        for (String rawQuery : stateObj.rawQueries) {
            try {
                boolean queryAnswer = RC_Binary_Indexing.RationalClosureJoelBinarySearchIndexing(
                        stateObj.ranked_KB,
                        rawQuery);
                blackhole.consume(queryAnswer);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    // -----------------------------------------------------------------------------------------------------------
    // Model-based Ranked Model Construction Benchmarks:
    // -----------------------------------------------------------------------------------------------------------

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void modelbased_construct_ranked_model_RC(StateObj stateObj, Blackhole blackhole)
            throws InterruptedException {
        RankedInterpretation RC_Minimal_Model = new RankedInterpretation(com.mbdr.modelbased.RationalClosure
                .ConstructRankedModel(stateObj.knowledgeBase, stateObj.KB_U));
        blackhole.consume(RC_Minimal_Model); // consume to avoid dead code elimination just in case?
    }

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void modelbased_construct_ranked_model_RC_BR(StateObj stateObj,
            Blackhole blackhole)
            throws InterruptedException {
                RankedInterpretation RC_Minimal_Model = new RankedInterpretation(com.mbdr.modelbased.RationalClosure
                .ConstructRankedModelBaseRank(stateObj.knowledgeBase, stateObj.KB_U));
        blackhole.consume(RC_Minimal_Model); // consume to avoid dead code elimination just in case?
    }

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void modelbased_construct_ranked_model_LC(StateObj stateObj, Blackhole blackhole)
            throws InterruptedException {
            RankedInterpretation RC_Minimal_Model = new RankedInterpretation(com.mbdr.modelbased.RationalClosure
                .ConstructRankedModel(stateObj.knowledgeBase, stateObj.KB_U));
        RankedInterpretation LC_Minimal_Model = com.mbdr.modelbased.LexicographicClosure
                .refine(stateObj.knowledgeBase, RC_Minimal_Model);
        blackhole.consume(LC_Minimal_Model); // consume to avoid dead code elimination just in case?
    }

    // -----------------------------------------------------------------------------------------------------------
    // Model-based Rational Closure Entailment Benchmarks:
    // -----------------------------------------------------------------------------------------------------------

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void modelbased_entailment_checker_RC(StateObj stateObj, Blackhole blackhole) throws InterruptedException {

        MinimalRankedEntailmentChecker rcChecker = new MinimalRankedEntailmentChecker(stateObj.rationalModel);

        for (String rawQuery : stateObj.rawQueries) {
            try {
                boolean queryAnswer = rcChecker.query(rawQuery);
                blackhole.consume(queryAnswer);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

    }

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void modelbased_entailment_checker_LC(StateObj stateObj, Blackhole blackhole) throws InterruptedException {

        MinimalRankedEntailmentChecker rcChecker = new MinimalRankedEntailmentChecker(stateObj.lexicographicModel);

        for (String rawQuery : stateObj.rawQueries) {
            try {
                boolean queryAnswer = rcChecker.query(rawQuery);
                blackhole.consume(queryAnswer);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

    }

    public static void main(String[] args) throws RunnerException {

        // TODO: Integrate full CLI interface using scaffold of BenchMarkCLI

        // ------------------------------------------------------------
        // Expected arguments:
        // ------------------------------------------------------------
        // args[0] = benchmark group index to indicate which set of benchmarks to run
        // (0-3 at the moment)
        // ------------------------------------------------------------
        // Benchmark group indices:
        // ------------------------------------------------------------
        // BaseRank Ranked Knowledge Base Benchmarks - 0
        // Formula-based Rational Closure Entailment Benchmarks - 1
        // Model-based Ranked Model Construction Benchmarks - 2
        // Model-based Rational Closure Entailment Benchmarks - 3
        // ------------------------------------------------------------

        final String KB_DIRECTORY = "knowledge_bases/Generated/";
        final String QUERY_FILE = "testingQueries.txt";

        System.out.println("Args:");
        for (String string : args) {
            System.out.println(string);
        }

        String include = "";

        switch (Integer.parseInt(args[0])) {
            case 0:
                include = "formulabased_baserank";
                break;
            case 1:
                include = "formulabased_RC";
                break;
            case 2:
                include = "modelbased_construct";
                break;
            case 3:
                include = "modelbased_entailment";
                break;
        }

        System.out.println("-----------------------------------------");
        System.out.println("Found files:");
        System.out.println("-----------------------------------------");
        ArrayList<String> fileNames = new ArrayList<>();
        File folder = new File(KB_DIRECTORY);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                System.out.println(file.getName());
                fileNames.add(file.getName());
            }
        }

        System.out.println("-----------------------------------------");
        System.out.println("Running benchmark harness...");
        System.out.println("-----------------------------------------");

        // DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        // Date date = new Date();

        Options opt = new OptionsBuilder()
                .include("^com.mbdr.benchmarking.BenchMark." + include + ".*")
                .forks(1)
                .param("kBDirectory", KB_DIRECTORY)
                .param("kBFileName", fileNames.toArray(new String[0]))
                .param("queriesFileName", QUERY_FILE)
                .param("benchmarkGroup", args[0])
                .resultFormat(ResultFormatType.CSV)
                // .result("benchmark_results/" + args[1] + "-" + dateFormat.format(date) +
                // ".csv")
                .result("benchmark_results/" + include + ".csv")
                .build();

        // new Runner(opt).list();
        Collection<RunResult> results = new Runner(opt).run();
        for (RunResult result : results) {
            Result<?> r = result.getPrimaryResult();
            System.out.println(r);
        }

    }

}