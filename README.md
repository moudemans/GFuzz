GDBFuzz
--- 

This repo has been created to store the code written for the MSc thesis at the TU Delft. The main focus is to create a schema aware fuzzer, specifically designed for applicaitons with a graph Database backend.

The codebase has been copied from the JQF+Zest [repository](https://github.com/rohanpadhye/JQF), publicly available on github.


## Overview repository

---
Below is a description of the modules currently in the project and their purpose:
- benchmarks - This module contains the benchmark programs used to evaluate the fuzzer
- jqf - This module contains the fuzzing framework. It is collected from the JQF repo mentioned above and contains the fuzz loop, mutators and guidance. The guidance for this thesis project can be found in this [folder](jqf/fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/mo/NoGuidance.java)
- mygraph - graph structure used in the benchmarks and mutators
- tutorial - tutorial collected from the JQF repo and toy examples

 
## How to run
The following steps describe how the fuzz loop can be run on a program. For the fuzz loop we need the fuzz framework to be built by maven, have a application on which we want to run the fuzzer and start the application with our bash script. 

**maven build**
If any changes have been made to the fuzzing framework (e.g. guidance, mutations, coverage), then the framework has to be built again with the following command:
``` bash
mvn package
```

**Test application**
The test application needs to be annotated with the tags @RunWith(JQF.class) and @Fuzz, as shown below:
```java
@RunWith(JQF.class)
public class ExampleTestClass {

    @Fuzz
    public void ExampleTestMethod () {
        ...
    }
}
```
The java files then need to be compiled with the following command
```bash
javac -cp .:$(jqf/scripts/classpath.sh) ExampleTestClass.java
```

The fuzzing loop is then started with the following command:
```bash
../../../../jqf/bin/jqf-mo -c .:$(../../../../jqf/scripts/classpath.sh) ExampleTestClass ExampleTestMethod
```


## How to run - toy example
Created a [directory](tutorial/src/main/java/) with the first tutorial. Tutorial can be found [here](https://github.com/rohanpadhye/JQF/wiki/Fuzzing-with-Zest#step-5-fuzz-with-zest)
- [Logic](tutorial/src/main/java/CalendarLogic.java)
- [Test](tutorial/src/test/java/CalendarTest.java)
- [Generator](tutorial/src/main/java/CalendarGenerator.java)

Compile by:
```bash
PATH1="tutorial/src/main/java/"

javac -cp .:$(jqf/scripts/classpath.sh) ${PATH1}CalendarLogic.java ${PATH1}CalendarGenerator.java ${PATH1}CalendarTest.java
```
Build fuzz framework by:

``` bash
mvn package
```

Run by going into specific directory, and running command below:
```bash
cd tutorial/src/test/java/
java -cp .:$(jqf/scripts/classpath.sh) org.junit.runner.JUnitCore CalendarTest
```


Fuzz with zest:
```bash
../../../../jqf/bin/jqf-zest -c .:$(../../../../jqf/scripts/classpath.sh) CalendarTest testLeapYear
```

A new guidance file has been made [here](jqf/fuzz/src/main/java/edu/berkeley/cs/jqf/fuzz/mo/)
the program can be run with:
```bash
../../../../jqf/bin/jqf-mo -c .:$(../../../../jqf/scripts/classpath.sh) CalendarTest testLeapYear
```

***
New file with integers:
```
PATH1="tutorial/src/main/java/"
FILE1="int"
javac -cp .:$(jqf/scripts/classpath.sh) ${PATH1}${FILE1}Logic.java ${PATH1}${FILE1}Generator.java ${PATH1}${FILE1}Test.java
```



---
 

