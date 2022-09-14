# Model-based Defeasible Reasoning Platform

A platform for implementing and benchmarking defeasible reasoning algorithms, including current approaches for rational and lexicographic closure and new model-based implementations for rational closure, Lehmann's lexicographic closure, and Casini et al.'s count-based lexicographic closure.

## Requirements

- Maven 3.3+
- Java 15+

## Installation

To build the project, run:
```mvn package```

## Usage

*Note*: Knowledge bases should be placed in the `data/` directory.

### Testing

To run queries on knowledge bases using all the built-in reasoning services, excute the following command:
```java -cp target/MBDR-1.0.0-jar-with-dependencies.jar com.mbdr.App <filename> <query>```

E.g.
```java -cp target/MBDR-1.0.0-jar-with-dependencies.jar com.mbdr.App "penguins.txt" "p|~f"```

### Benchmarking

To run benchmarks for the implemented algorithms, run the following commands:

Construction:
```java -cp target/MBDR-1.0.0-jar-with-dependencies.jar com.mbdr.benchmarking.BenchmarkConstruction```

Entailment:
```java -cp target/MBDR-1.0.0-jar-with-dependencies.jar com.mbdr.benchmarking.BenchmarkEntailment```

## More Information

Visit the project website: https://twiddleproject.com, for more information.

## Authors
- Jaron Cohen
- Carl Combrinck
