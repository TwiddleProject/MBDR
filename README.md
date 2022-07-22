# Model-based Defeasible Reasoner

Provides entailment checking services for Rational and Lexicographic Closure of a knowledge base using ranked models. Construction of these models and entailment checking is benchmarked against exisiting formula-based approaches.

## Requirements

- Maven 3.3+
- Java 15+

## Installation

To build the project, run:
```mvn package```

## Usage

To execute the application, run:
```java -cp target/MBDR-1.0.0-jar-with-dependencies.jar com.mbdr.App <filename> <query>```

To execute the benchmarks, run:

Construction:
```java -cp target/MBDR-1.0.0-jar-with-dependencies.jar com.mbdr.benchmarking.BenchmarkConstruction```

Entailment:
```java -cp target/MBDR-1.0.0-jar-with-dependencies.jar com.mbdr.benchmarking.BenchmarkEntailment```

## Authors
- Jaron Cohen
- Carl Combrinck
