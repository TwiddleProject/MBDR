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


### Web Server

To run the Javalin web application:

```java -cp target/MBDR-1.0.0-jar-with-dependencies.jar com.mbdr.web.WebApp```

### Knowledge Base Generation

Knowledge base generation requires the latest version of scala.

The benchmarking data set was generated using the Knowledge Base Generation tool from SCADR (2021). It can be generated using `KBGenerationScript.sh` in the `scripts/knowledge_base_generation/` directory.

### Graphing

Scripts for graphing construction and entailment results are included in the `scripts/graphing/` directory.

Install the required modules before running:
`pip install -r requirement.txt`

### Javadoc Generation

To generate the javadocs, run the following command in a Linux terminal/shell:
`javadoc -sourcepath ./src/main/java/com/mbdr -d ./docs -classpath target/MBDR-1.0.0-jar-with-dependencies.jar $(find ./src/main/java/com/mbdr/ -name *.java)`

## More Information

Visit the project website: https://twiddleproject.com, for more information.

## Authors
- Jaron Cohen
- Carl Combrinck
