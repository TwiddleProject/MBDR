package com.mbdr.benchmarking;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "Benchmark", mixinStandardHelpOptions = true, version = "Benchmark 1.0.0", description = "Provides an automated benchmarking pipeline for the MBDR project.")
public class BenchMarkCLI implements Runnable {

    @Option(names = { "-s", "--font-size" }, description = "Font size")
    int fontSize = 14;

    @Parameters(paramLabel = "<word>", defaultValue = "Hello, Jaron", description = "Words to be translated into ASCII art.")
    private String[] words = { "Hello,", "Jaron" };

    @Override
    public void run() {
        System.out.println("Hello!");
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new BenchMarkCLI()).execute(args);
        System.exit(exitCode);
    }

}
