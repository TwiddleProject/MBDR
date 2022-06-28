package com.mbdr.benchmarking;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.infra.Blackhole;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlFormula;
import org.tweetyproject.logics.pl.syntax.PlSignature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.mbdr.utils.parsing.*;
import com.mbdr.structures.*;

import com.mbdr.formulabased.BaseRank;
import com.mbdr.modelbased.EntailmentChecker;

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

        @Param({ "modelbased" })
        String benchmarkGroup;

        KnowledgeBase knowledgeBase;
        ArrayList<PlBeliefSet> ranked_KB;
        Set<NicePossibleWorld> KB_U;
        ArrayList<Set<NicePossibleWorld>> RC_Minimal_Model;
        ArrayList<Set<NicePossibleWorld>> LC_Minimal_Model;
        ArrayList<String> rawQueries;

        @Setup(Level.Trial) // Level.Trial is the default level. The method is called once for each full run
        // of the benchmark
        public void setup() {
            System.out.println("*****State initialization happening here****");

            System.out.println("reading in:\t" + kBFileName);
            KnowledgeBaseReader reader = new KnowledgeBaseReader(kBDirectory);

            try {
                // Read in the knowledge base and store in state object
                ArrayList<String> rawFormulas = reader.readFormulasFromFile(kBFileName);
                this.knowledgeBase = Parser.parseFormulas(rawFormulas);

                // Rank the knowledge base using baserank - for benchmarking formulabased
                // entailment checking
                this.ranked_KB = BaseRank.BaseRankDirectImplementation(this.knowledgeBase);

                // Enumerate all possible worlds w.r.t. atoms of KB to reduce benchmarking
                // runtime across repeated runs
                PlSignature KB_atoms = this.knowledgeBase.union().getMinimalSignature();
                System.out.print("Getting all possible worlds w.r.t. atoms:\t" + KB_atoms);
                this.KB_U = NicePossibleWorld.getAllPossibleWorlds(KB_atoms.toCollection());

                // Construct the ranked models for RC and LC for query benchmarking
                this.RC_Minimal_Model = com.mbdr.modelbased.RationalClosure
                        .ConstructRankedModel(knowledgeBase, this.KB_U);
                this.LC_Minimal_Model = com.mbdr.modelbased.LexicographicClosure
                        .refine(knowledgeBase, RC_Minimal_Model);

                System.out.println("reading in:\t" + queriesFileName);
                // Read in all the queries from the query file
                this.rawQueries = reader.readFormulasFromFile(queriesFileName);

            } catch (Exception e) {
                // TODO: handle exception
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
    public void formulabased_baserank_direct_implementation(StateObj stateObj, Blackhole blackhole)
            throws InterruptedException {
        ArrayList<PlBeliefSet> ranked_KB = BaseRank.BaseRankDirectImplementation(stateObj.knowledgeBase);
        blackhole.consume(ranked_KB);
    }

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void formulabased_RC_Joel_Regular(StateObj stateObj, Blackhole blackhole) throws InterruptedException {

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
    public void modelbased_construct_ranked_model_RC(StateObj stateObj, Blackhole blackhole)
            throws InterruptedException {
        ArrayList<Set<NicePossibleWorld>> RC_Minimal_Model = com.mbdr.modelbased.RationalClosure
                .ConstructRankedModel(stateObj.knowledgeBase, stateObj.KB_U);
        blackhole.consume(RC_Minimal_Model); // consume to avoid dead code elimination just in case?
    }

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void modelbased_construct_ranked_model_LC(StateObj stateObj, Blackhole blackhole)
            throws InterruptedException {
        ArrayList<Set<NicePossibleWorld>> RC_Minimal_Model = com.mbdr.modelbased.RationalClosure
                .ConstructRankedModel(stateObj.knowledgeBase, stateObj.KB_U);
        ArrayList<Set<NicePossibleWorld>> LC_Minimal_Model = com.mbdr.modelbased.LexicographicClosure
                .refine(stateObj.knowledgeBase, RC_Minimal_Model);
        blackhole.consume(LC_Minimal_Model); // consume to avoid dead code elimination just in case?
    }

    @Benchmark
    @Fork(value = 2) // 2 trials in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void modelbased_entailment_checker_RC(StateObj stateObj, Blackhole blackhole) throws InterruptedException {

        EntailmentChecker rcChecker = new EntailmentChecker(stateObj.RC_Minimal_Model);

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

        EntailmentChecker rcChecker = new EntailmentChecker(stateObj.LC_Minimal_Model);

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

        // Expected arguments:
        // args[0] = directory where knowledge base textfiles are located
        // args[1] = flag to indicate which set of benchmarks to run (currently two
        // flags: "modelbased" or "formulabased")

        System.out.println("Args:");
        for (String string : args) {
            System.out.println(string);
        }

        System.out.println("-----------------------------------------");
        System.out.println("Found files:");
        System.out.println("-----------------------------------------");
        ArrayList<String> fileNames = new ArrayList<>();
        File folder = new File(args[0]);
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

        Options opt = new OptionsBuilder()
                // .include(BenchMark.class.getSimpleName())
                .include("^com.mbdr.benchmarking.BenchMark." + args[1] + ".*")
                .forks(1)
                .param("kBDirectory", args[0])
                .param("kBFileName", fileNames.toArray(new String[0]))
                .build();

        // new Runner(opt).list();
        new Runner(opt).run();

    }

}
