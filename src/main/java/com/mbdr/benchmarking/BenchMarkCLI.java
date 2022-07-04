package com.mbdr.benchmarking;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.RunAll;
import picocli.CommandLine.ArgGroup;

import org.openjdk.jmh.runner.RunnerException;

import com.mbdr.benchmarking.*;

public class BenchMarkCLI {

    @Command(name = "Benchmark", subcommands = { SubcommandFormulabased.class, SubcommandModelbased.class,
            CommandLine.HelpCommand.class }, version = "Benchmark 1.0.0", description = "Benchmark utility for the MBDR project 2022.")
    static class ParentCommand implements Runnable {

        @Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
        private boolean helpRequested = false;

        @Override
        public void run() {
        }
    }

    @Command(name = "formulabased", description = "Run benchmarks that relate to the SCADR project from 2021.")
    static class SubcommandFormulabased implements Runnable {

        @ArgGroup(exclusive = false, heading = "Required options for Baserank benchmarks.%n")
        BaseRankBenchmarkOptions dependentBR;

        static class BaseRankBenchmarkOptions {
            @Option(names = {
                    "-baserank" }, required = true, description = "Flag to indicate to utility to benchmark baserank algorithms.")
            boolean baserank;
            @Option(names = { "-d",
                    "--directory" }, required = true, description = "Path to directory containing the knowledgebase textfiles.")
            String directory;
            @Option(names = { "-o",
                    "--output" }, required = true, description = "Name of the file to output benchmark results.")
            String output;
        }

        @Override
        public void run() {
            System.out.println("Run some formulabased benchmarks...");
            System.out.println("-baserank:\t" + dependentBR.baserank);
            System.out.println("-d:\t" + dependentBR.directory);
            System.out.println("-o:\t" + dependentBR.output);

        }
    }

    @Command(name = "modelbased", description = "Run benchmarks that relate to the MBDR project 2022.")
    static class SubcommandModelbased implements Runnable {

        // @Parameters(arity = "1..*", paramLabel = "<language code>", description =
        // "language code(s) to be resolved")
        // private String[] languageCodes;

        @Override
        public void run() {
            System.out.println("Run some modelbased benchmarks...");
            // for (String code : languageCodes) {
            // System.out.println(String.format("%s: %s", code.toLowerCase(), new
            // Locale(code).getDisplayLanguage()));
            // }
        }
    }

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new ParentCommand());
        cmd.setExecutionStrategy(new RunAll()); // default is RunLast
        cmd.execute(args);

        if (args.length == 0) {
            cmd.usage(System.out);
        }
    }
}
