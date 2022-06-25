package com.mbdr.benchmarking;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.infra.Blackhole;

import org.tweetyproject.logics.pl.semantics.NicePossibleWorld;
import org.tweetyproject.logics.pl.syntax.PlBeliefSet;
import org.tweetyproject.logics.pl.syntax.PlSignature;

import java.io.File;
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

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BenchMark {

    public final static String KNOWLEDGE_BASE_DIR = "knowledge_bases/Joel/ranks10/";

    @State(Scope.Benchmark) // all threads running the benchmark share the same state object.
    public static class StateObj {

        KnowledgeBase knowledgeBase;
        Set<NicePossibleWorld> KB_U;

        @Setup(Level.Trial) // Level.Trial is the default level. The method is called once for each full run
                            // of the benchmark
        public void setup() {
            System.out.println("*****State initialization happening here****");

            String fileName = "testing.txt";

            System.out.println("reading in:\t" + fileName);
            KnowledgeBaseReader reader = new KnowledgeBaseReader(KNOWLEDGE_BASE_DIR);

            try {
                ArrayList<String> rawFormulas = reader.readFormulasFromFile(fileName);
                this.knowledgeBase = Parser.parseFormulas(rawFormulas);
                PlSignature KB_atoms = this.knowledgeBase.union().getMinimalSignature();
                System.out.print("Getting all possible worlds w.r.t. atoms:\t" + KB_atoms);
                this.KB_U = NicePossibleWorld.getAllPossibleWorlds(KB_atoms.toCollection());
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
    @Fork(value = 2) // 2 trails in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void baseRankDirectImplementation(StateObj stateObj, Blackhole blackhole) throws InterruptedException {
        ArrayList<PlBeliefSet> ranked_KB = BaseRank.BaseRankDirectImplementation(stateObj.knowledgeBase);
        blackhole.consume(ranked_KB);
    }

    @Benchmark
    @Fork(value = 2) // 2 trails in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void constructRankedModelRC(StateObj stateObj, Blackhole blackhole) throws InterruptedException {
        ArrayList<Set<NicePossibleWorld>> RC_Minimal_Model = com.mbdr.modelbased.RationalClosure
                .ConstructRankedModel(stateObj.knowledgeBase, stateObj.KB_U);
        blackhole.consume(RC_Minimal_Model); // consume to avoid dead code elimination just in case?
    }

    @Benchmark
    @Fork(value = 2) // 2 trails in total
    @Measurement(iterations = 10, time = 1) // 10 iterations
    @Warmup(iterations = 5, time = 1) // 5 iterations of warmup
    public void constructRankedModelLC(StateObj stateObj, Blackhole blackhole) throws InterruptedException {
        ArrayList<Set<NicePossibleWorld>> RC_Minimal_Model = com.mbdr.modelbased.RationalClosure
                .ConstructRankedModel(stateObj.knowledgeBase, stateObj.KB_U);
        ArrayList<Set<NicePossibleWorld>> LC_Minimal_Model = com.mbdr.modelbased.LexicographicClosure
                .refine(stateObj.knowledgeBase, RC_Minimal_Model);
        blackhole.consume(LC_Minimal_Model); // consume to avoid dead code elimination just in case?
    }

    public static void main(String[] args) throws RunnerException {

        // ArrayList<String> fileNames = new ArrayList<>();
        // File folder = new File(KNOWLEDGE_BASE_DIR);
        // File[] listOfFiles = folder.listFiles();

        // for (File file : listOfFiles) {
        // if (file.isFile()) {
        // // System.out.println(file.getName());
        // fileNames.add(file.getName());
        // }
        // }

        Options opt = new OptionsBuilder()
                .include(BenchMark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}
