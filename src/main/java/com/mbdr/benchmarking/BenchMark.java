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
import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlSignature;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.mbdr.utils.parsing.*;
import com.mbdr.common.services.DefeasibleReasoner;
import com.mbdr.common.structures.*;
import com.mbdr.formulabased.construction.BaseRank;
import com.mbdr.formulabased.reasoning.RationalBinaryReasoner;
import com.mbdr.formulabased.reasoning.RationalBinaryIndexingReasoner;
import com.mbdr.formulabased.reasoning.RationalDirectReasoner;
import com.mbdr.formulabased.reasoning.RationalIndexingReasoner;
import com.mbdr.formulabased.reasoning.RationalRegularReasoner;
import com.mbdr.modelbased.construction.LexicographicCountModelRank;
import com.mbdr.modelbased.construction.ModelBaseRank;
import com.mbdr.modelbased.construction.ModelRank;
import com.mbdr.modelbased.reasoning.MinimalRankedEntailmentReasoner;
import com.mbdr.modelbased.structures.RankedInterpretation;

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
                this.knowledgeBase = reader.parse(kBFileName);

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
                    this.ranked_KB = new BaseRank().construct(this.knowledgeBase);

                    System.out.println("reading in:\t" + queriesFileName);
                    // Read in all the queries from the query file

                    KnowledgeBaseReader readerTemp = new KnowledgeBaseReader("knowledge_bases/");

                    try {
                        this.rawQueries = readerTemp.readFileLines(queriesFileName);
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
                    this.rationalModel = new ModelRank().construct(knowledgeBase);

                    this.lexicographicModel = new LexicographicCountModelRank(rationalModel)
                            .construct(knowledgeBase);

                    System.out.println("reading in:\t" + queriesFileName);
                    // Read in all the queries from the query file

                    KnowledgeBaseReader readerTemp2 = new KnowledgeBaseReader("knowledge_bases/");

                    try {
                        this.rawQueries = readerTemp2.readFileLines(queriesFileName);
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
        ArrayList<PlBeliefSet> ranked_KB = new BaseRank().construct(stateObj.knowledgeBase);
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

        DefeasibleReasoner rcDirect = new RationalDirectReasoner(stateObj.ranked_KB, stateObj.knowledgeBase);
        for (String rawQuery : stateObj.rawQueries) {
            try {
                boolean queryAnswer = rcDirect.query(rawQuery);
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

        DefeasibleReasoner rcRegular = new RationalRegularReasoner(stateObj.ranked_KB);
        for (String rawQuery : stateObj.rawQueries) {
            try {
                boolean queryAnswer = rcRegular.query(rawQuery);
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

        DefeasibleReasoner rcBinary = new RationalBinaryReasoner(stateObj.ranked_KB);

        for (String rawQuery : stateObj.rawQueries) {
            try {
                boolean queryAnswer = rcBinary.query(rawQuery);
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

        DefeasibleReasoner rcIndex = new RationalIndexingReasoner(stateObj.ranked_KB);

        for (String rawQuery : stateObj.rawQueries) {
            try {
                boolean queryAnswer = rcIndex.query(rawQuery);
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

        DefeasibleReasoner rcBinaryIndex = new RationalBinaryIndexingReasoner(stateObj.ranked_KB);

        for (String rawQuery : stateObj.rawQueries) {
            try {
                boolean queryAnswer = rcBinaryIndex.query(rawQuery);
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
        RankedInterpretation RC_Minimal_Model = new ModelRank().construct(stateObj.knowledgeBase);
        blackhole.consume(RC_Minimal_Model); // consume to avoid dead code elimination just in case?
    }

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void modelbased_construct_ranked_model_RC_BR(StateObj stateObj,
            Blackhole blackhole)
            throws InterruptedException {
                RankedInterpretation RC_Minimal_Model = new ModelBaseRank().construct(stateObj.knowledgeBase);
        blackhole.consume(RC_Minimal_Model); // consume to avoid dead code elimination just in case?
    }

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void modelbased_construct_ranked_model_LC(StateObj stateObj, Blackhole blackhole)
            throws InterruptedException {
        RankedInterpretation LC_Minimal_Model = new LexicographicCountModelRank()
                .construct(stateObj.knowledgeBase);
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

        MinimalRankedEntailmentReasoner rcChecker = new MinimalRankedEntailmentReasoner(stateObj.rationalModel);

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

        MinimalRankedEntailmentReasoner rcChecker = new MinimalRankedEntailmentReasoner(stateObj.lexicographicModel);

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
                .result("results/" + include + ".csv")
                .build();

        // new Runner(opt).list();
        Collection<RunResult> results = new Runner(opt).run();
        for (RunResult result : results) {
            Result<?> r = result.getPrimaryResult();
            System.out.println(r);
        }

    }

}