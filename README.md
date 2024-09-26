PGFuzz
--- 

This repo contains PGFuzz, a coverage-guided, schema-aware fuzzer for graph processing applications. 


## Contents of the repository

---
Below is a description of the directories found in the repository root:

- [benchmarks](benchmarks) - This module contains the benchmark programs used to evaluate the fuzzer
- [benchmarksFuzzable](benchmarksFuzzable) - This folder is automatically generated from the benchmarks folder and
  contains the compiled code from benchmarks and fuzzing directories (containting seeds and results)
- graph generators - This folder contains the clones of two state-of-the-art graph generators:
	- The GMark graph generator cloned from the [GMark repository](https://github.com/gbagan/gmark) (commit 77be5b6).
	- The PGMark graph generator from the [PGMark](https://github.com/ThomHurks/pgMark). We extended PGMark so that it also supports edge properties to be generated.
- [jqf](jqf) - This module contains the JQF fuzzing framework collected from [JQF+Zest](https://github.com/rohanpadhye/JQF), which provides a generic fuzz loop, mutators and guidance. The contributions made for the PGFuzz project can be found in this [folder](jqf/fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/mo)
- [mygraph](mygraph) - This module contains the Graph API used in PGFuzz and the 
  mutations supported by PGFuzz. 
  - [Graph API](mygraph/src/main/java/tudcomponents)
  - [Mutations](mygraph/src/main/java/tudgraphs)

## Building PGFuzz 

We follow the following steps to run PGFuzz to test a benchmark application:  
- Building GMark and PGMark graph generators   
- Packaging JQF framework and PGFuzz source code, managed by maven  
- Compiling java benchmark applications with jqf annotations  
- Generating seed input files, located in the fuzz-dir of the application being tested 
- Running the PGFuzz grey-box fuzzer graph generator 

The reproduction package provides script files to help build and run PGFuzz.

### Building the graph generators:

**Build GMark graph generator** in `graphGenerators/gmark` (a clone of [GMark](https://github.com/gbagan/gmark)). This command should produce the `test` executable in `graphGenerators/gmark/src/`.

```bash 
cd graphGenerators/gmark/demo/scripts
./compile-all.sh
```

**Build PGMark graph generator** in `graphGenerators/pgMark` (an extended version of the [PGMark](https://github.com/ThomHurks/pgMark)). 

```
cd graphGenerators/pgMark
ccmake .
```

Then press 'c' to configure and press 'g' to generate and exit. Then run `make`. This command should produce the executable `graphGenerators/pgMark/bin/pgMark`.


```
make
```



### Package the PGFuzz project:
 
Package the project with the following command:
```bash 
mvn package
```

### Prepare benchmarks:

Prepare each benchmark to be tested with the fuzzer using the following command: 

```bash 
bash ./scripts/prepareBenchmark.sh <PATH> <program_name>
# example:
# bash ./scripts/prepareBenchmark.sh "benchmarks/src/main/java/P1Medical/" "P1"
```
Where <PATH> is the path to the application that should be compiled and <program_name> the name 
of the output folder.


## Running PGFuzz 

### Run graph generators:

To run PGMark, run the following command

```bash 
scripts/generator/runPGMarkGraphGenerator.sh <program_name> <graph_size>
# Example:
# scripts/generator/runPGMarkGraphGenerator.sh P2 100
```
To run GMark, run the following command

```bash 
scripts/generator/runGMarkGraphGenerator.sh <program_name> <graph_size>
# Example:
# scripts/generator/runGMarkGraphGenerator.sh A1 500
```
Both generators check the new-input dir in the fuzz-dir to see if there are enough new inputs. If 
not, new inputs are generated.
A schema needs to be provided in the folder: 

/benchmarksFuzzable/<program_name>/fuzz-dir/GenSchema.xml

**Run the fuzzing loop**:

Run the fuzzing loop with the following command

```bash 
scripts/runBenchmark.sh 
    <number_repetitions> 
    <output_name> 
    <program_name> 
    <mutation_method>
    <new_inputs_enables> 
    <duration>
    <mutation_category>
    <mutation_depth> 
    
# exaple:
# scripts/runBenchmark.sh 5 test_save P2 PGFuzz 1 d5 -1 100
```
where :
- number repetitions = amount of times the fuzz process is repeated for independent results
- output name = name of directory where the independent runs are stored
- program_name= name of the program that should be tested, located in benchmarksFuzzable
- mutation_method= Fuzz method
  - -1 | None = no mutation
  - 0 | Random = Random
  - 1 | PGFuzz = PGFuzz mutations
- new_inputs = allow new inputs
  - 0 = No
  - 1 = Yes
- duration = Limit for how long the fuzz loop should run
  - d\<x> = duration of x in minutes
  - t\<x> = amount of trials x
- Mutation_category = Enable single specific mutation category
  - -1 = Use all mutations
  - 0 = Use category 1
  - 1 = Use category 2
  - ...
  - 4 = Use category 5
- mutation_depth = maximum amount of times an input is mutated


## Graph generators
The graph generators can also be used to generate a single graph. See below:

### Run GMark
````bash
scripts/generator/gMarkGenerateSingleGraph.sh "" 100 3 default/output2/
````
### Run PGMark 
```bash
scripts/generator/pgMarkGenerateSingleGraph.sh "" 100 3 default/output2/
```

where

``` 
scripts/pgMarkGenerateGraph.sh <Schema> <graph size> <DS count> <output dir> <output file>
```

## Make changes, extend benchmarks

The following steps describe how the fuzz loop can be run on a program. For the fuzz loop we need
the fuzz framework to be built by maven, have an application on which we want to run the fuzzer and
start the application with our bash script.

**maven build**
If any changes have been made to the fuzzing framework (e.g. guidance, mutations, coverage), then
the framework has to be built again with the following command:

``` bash
mvn package
```

**Test application**
The test application needs to be annotated with the tags @RunWith(JQF.class) and @Fuzz, as shown
below:

```java

@RunWith(JQF.class)
public class ExampleTestClass {

    @Fuzz
    public void ExampleTestMethod() {
        // ...
    }
}
```

The java files then need to be compiled with the following command

```bash
javac -cp .:$(jqf/scripts/classpath.sh) ExampleTestClass.java
```

The fuzzing loop is then started with the following command from the location the compiled program:

```bash
../../../../jqf/bin/jqf-mo -c .:$(../../../../jqf/scripts/classpath.sh) ExampleTestClass ExampleTestMethod
```


### Notes on frequently problems

- Tutorials/example programs you want to test should not have a package defined, the class loader is
  not able to find it then. Scripts provided that prepare a benchmark remove the first line, 
  assuming the package is defined there.
- If there is a stack overflow of the heap size. It can be adjusted 
  [here](jqf/scripts/jqf-driver.sh)
- If the benchmarks require the mygraph framework, add it with autocomplete and then remove it from
  the pom on project level because there will be a cycle detected otherwise.

---
 
## Acknowledgement

- PGFuzz has been created on top of  [JQF+Zest](https://github.com/rohanpadhye/JQF), specifically
  created for researchers to build their new fuzz methods and publicly available on github.
- The Graph generator GMark has been copied to this project in order to run its code. The repo
  it is collected from can be found [here](https://github.com/gbagan/gmark) and has been
  acknowledged in the thesis report
- The graph generator [PGMark](https://github.com/ThomHurks/pgMark) (extension of GMark) has been
  copied from the publicly available
  repo and extended s.t. edge properties can also be generated
