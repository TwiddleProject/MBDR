package com.mbdr.benchmarking;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BenchMark {

    @State(Scope.Benchmark) // all threads running the benchmark share the same state object.
    public static class StateObj {

        @Setup(Level.Trial) // Level.Trial is the default level. The method is called once for each full run
                            // of the benchmark
        public void setup() {
            System.out.println("*****State initialization happening here****");
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
    public void benchmark() throws InterruptedException {
        // this method was intentionally left blank.
        Thread.sleep(100);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchMark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

}
